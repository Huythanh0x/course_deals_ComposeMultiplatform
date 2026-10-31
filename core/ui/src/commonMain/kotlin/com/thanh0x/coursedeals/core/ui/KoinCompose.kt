package com.thanh0x.coursedeals.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.koin.core.parameter.ParametersDefinition
import org.koin.mp.KoinPlatform

@Composable
inline fun <reified T : ViewModel> koinViewModel(
    key: Any? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val vm = remember(key) {
        if (parameters != null) {
            KoinPlatform.getKoin().get<T>(parameters = parameters)
        } else {
            KoinPlatform.getKoin().get<T>()
        }
    }
    DisposableEffect(vm) {
        onDispose {
            // Keep ViewModels alive across recompositions inside the same route
        }
    }
    return vm
}
