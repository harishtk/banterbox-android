package space.banterbox.app.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.BLUE_DOMINATED_EXAMPLE

/**
 * Multipreview annotation that represents light and dark themes. Add this annotation to a
 */
@Preview(name = "Light Default", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Default", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Android", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Android", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Dynamic Light", uiMode = Configuration.UI_MODE_NIGHT_NO, wallpaper = BLUE_DOMINATED_EXAMPLE)
@Preview(name = "Dynamic Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, wallpaper = BLUE_DOMINATED_EXAMPLE)
annotation class ThemePreviews