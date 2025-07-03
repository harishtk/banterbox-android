package space.banterbox.app.feature.home.presentation.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import space.banterbox.app.common.util.UiText

@Stable
data class SettingsItem(
    val settingsListType: SettingsListType,
    val id: Int,
    val title: UiText,
    @DrawableRes val icon: Int?,
    val description: UiText?,
    val hasMore: Boolean = false
)

enum class SettingsListType {
    SIMPLE
}