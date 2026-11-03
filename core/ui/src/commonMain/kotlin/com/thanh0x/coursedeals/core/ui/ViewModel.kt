package com.thanh0x.coursedeals.core.ui

import com.thanh0x.coursedeals.core.logging.AppLogger
import com.thanh0x.coursedeals.core.logging.CommonTracingInitializer
import com.thanh0x.coursedeals.core.logging.TracingReporter
import com.thanh0x.coursedeals.core.logging.error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class ViewModel(
    private val mainDispatcher: CoroutineDispatcher? = null,
) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleCoroutineException(throwable)
    }

    val viewModelScope: CoroutineScope by lazy {
        CoroutineScope(
            SupervisorJob() + (mainDispatcher ?: Dispatchers.Main.immediate) + coroutineExceptionHandler
        )
    }

    internal fun handleCoroutineException(throwable: Throwable) {
        val vmName = this::class.simpleName ?: "ViewModel"
        val tags = mapOf(
            "viewModel" to vmName,
            "currentRoute" to CommonTracingInitializer.currentRoute,
            "isGuest" to CommonTracingInitializer.isGuest.toString(),
            "networkType" to CommonTracingInitializer.networkType,
        )
        AppLogger.tag(vmName).error(throwable) {
            "Uncaught ViewModel coroutine exception in $vmName (route=${CommonTracingInitializer.currentRoute})"
        }
        TracingReporter.captureException(
            throwable = throwable,
            tags = tags,
            isFatal = false,
        )
        onUncaughtException(throwable)
    }

    protected open fun onUncaughtException(throwable: Throwable) = Unit

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
