package com.itbenevides.genesys21

import androidx.compose.ui.window.ComposeUIViewController
import com.itbenevides.genesys21.di.initialDeepLink

fun MainViewController() = ComposeUIViewController { App(initialDeepLink = initialDeepLink) }
