package space.banterbox.app.core.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import space.banterbox.app.common.util.UiText

@Stable
class BanterboxTopAppBarState(
    val title: String,
    val showMoreOptions: Boolean = false,
    val onMoreOptionsClick: () -> Unit = {},
    val showNavigationIcon: Boolean = false,
    val onNavigationIconClick: () -> Unit = {},
    val showNotificationIcon: Boolean = false,
    val notificationBadgeCount: Int? = null,
    val onNotificationIconClick: () -> Unit = {},
    val showSettingsIcon: Boolean = false,
    val onSettingsIconClick: () -> Unit = {},
    val showActionText: Boolean = false,
    val actionText: UiText? = null,
    @DrawableRes val actionLeadingIcon: Int? = null,
    val onActionTextClick: () -> Unit = {}
)
