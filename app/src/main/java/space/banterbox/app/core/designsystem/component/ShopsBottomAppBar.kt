package space.banterbox.app.core.designsystem.component

import androidx.compose.runtime.Stable

@Stable
data class BanterboxBottomAppBarVisibilityState(
    val hidden: Boolean
) {
    companion object {
        internal val Default = BanterboxBottomAppBarVisibilityState(
            hidden = true
        )
    }
}