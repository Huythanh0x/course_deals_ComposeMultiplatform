package com.thanh0x.coursedeals.core.ui

import com.thanh0x.coursedeals.core.logging.CommonTracingInitializer
import com.thanh0x.coursedeals.core.logging.TracingReporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelErrorBoundaryTest {

    private class SampleTestViewModel(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ) : ViewModel(mainDispatcher = dispatcher) {
        val caughtThrowables = mutableListOf<Throwable>()
        var siblingCompleted = false

        fun launchFailingAndSiblingCoroutines() {
            viewModelScope.launch {
                throw IllegalArgumentException("Simulated ViewModel failure")
            }
            viewModelScope.launch {
                delay(10)
                siblingCompleted = true
            }
        }

        override fun onUncaughtException(throwable: Throwable) {
            caughtThrowables.add(throwable)
        }
    }

    @BeforeTest
    fun setUp() {
        CommonTracingInitializer.resetForTesting()
        CommonTracingInitializer.initialize(
            distroChannel = "unit-test",
            appVersion = "1.0.0 (105)",
        )
        CommonTracingInitializer.setCurrentRoute("detail/102")
        CommonTracingInitializer.setNetworkType("wifi")
    }

    @AfterTest
    fun tearDown() {
        CommonTracingInitializer.resetForTesting()
    }

    @Test
    fun viewModelCoroutineErrorBoundaryCatchesExceptionAndKeepsSupervisorScopeAlive() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = SampleTestViewModel(testDispatcher)

        viewModel.launchFailingAndSiblingCoroutines()
        advanceUntilIdle()

        // 1. Hook received the uncaught exception
        assertEquals(1, viewModel.caughtThrowables.size)
        assertEquals("Simulated ViewModel failure", viewModel.caughtThrowables.first().message)

        // 2. Sibling coroutine still completed because SupervisorJob did not cancel the scope
        assertTrue(viewModel.siblingCompleted)

        // 3. TracingReporter recorded the non-fatal exception with contextual tags
        val event = TracingReporter.capturedExceptions.lastOrNull()
        assertNotNull(event)
        assertFalse(event.isFatal)
        assertEquals("IllegalArgumentException", event.throwableClass)
        assertEquals("SampleTestViewModel", event.tags["viewModel"])
        assertEquals("detail/102", event.tags["currentRoute"])
        assertEquals("true", event.tags["isGuest"])
        assertEquals("wifi", event.tags["networkType"])

        viewModel.onCleared()
    }
}
