package com.redforge.app.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redforge.app.RedForgeApplication

/**
 * Small helper so every screen can do:
 *   val vm: HomeViewModel = redForgeViewModel { app -> HomeViewModel(app.splitRepository, ...) }
 * without pulling in a DI framework.
 *
 * Needs to be `inline`/`reified` because the underlying Compose `viewModel()`
 * function keys the ViewModel store by the reified type `T` — a plain
 * generic `T` here can't satisfy that.
 */
@Composable
inline fun <reified T : ViewModel> redForgeViewModel(crossinline factory: (RedForgeApplication) -> T): T {
    val app = LocalContext.current.applicationContext as RedForgeApplication
    return viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = factory(app) as VM
    })
}
