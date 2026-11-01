package com.termux.app.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Compile-time check for the Kotlin and Compose toolchain. Not referenced anywhere,
 * so it has no runtime effect. To be removed once real Compose code lands.
 */
@Suppress("unused")
@Composable
internal fun ComposeToolchainCheck() {
    Text("Termux Next")
}
