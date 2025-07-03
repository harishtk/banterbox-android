package space.banterbox.app.core.designsystem.component

import androidx.compose.runtime.Stable

@Stable
data class ShopsBottomAppBarVisibilityState(
    val hidden: Boolean
) {
    companion object {
        internal val Default = ShopsBottomAppBarVisibilityState(
            hidden = true
        )
    }
}