package github.tornaco.android.thanos.main

import android.content.Context
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import github.tornaco.android.thanos.module.compose.common.ComposeThemeActivity
import github.tornaco.android.thanos.util.ActivityUtils

@AndroidEntryPoint
class NavActivity2 : ComposeThemeActivity() {

    object Starter {
        @JvmStatic
        fun start(context: Context?) {
            ActivityUtils.startActivity(context, NavActivity2::class.java)
        }
    }

    override fun isF(): Boolean {
        return false
    }

    @Composable
    override fun Content() {
        NavScreen()
    }
}