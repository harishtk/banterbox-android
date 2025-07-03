package space.banterbox.app.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import space.banterbox.app.common.util.HapticUtil
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.animation.ShakeConfig
import space.banterbox.app.core.designsystem.component.animation.rememberShakeController
import space.banterbox.app.core.designsystem.component.animation.shake
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.insetLarge
import space.banterbox.app.ui.theme.BanterboxTheme
import kotlinx.coroutines.delay
import timber.log.Timber

enum class LoadingState {
    Idle, Loading, Success, Failed;

    val backgroundColor: Color
        @Composable get() =
            when (this) {
                Failed -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.primary
            }

    val foreGroundColor: Color
        @Composable get() =
            when (this) {
                Failed -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onPrimary
            }
}

class LoadingButtonState(
    initialValue: LoadingState = LoadingState.Idle,
) {
    var loadingState: LoadingState by mutableStateOf(initialValue)
    var enabled: Boolean by mutableStateOf(true)
}

@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    loadingButtonState: LoadingButtonState,
    text: String,
    resetDelay: Long = 1_000,
    onClick: () -> Unit = {},
    onResetRequest: (currentLoadingState: LoadingState) -> Unit = { loadingButtonState.loadingState = LoadingState.Idle },
) {
    val context = LocalContext.current
    val shakeController = rememberShakeController()
    var shouldIgnoreReset: Boolean = remember { false }

    Timber.d("LoadingButton: loadState=${loadingButtonState.loadingState}")

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val widthAnimationState = animateDpAsState(
            targetValue = when (loadingButtonState.loadingState) {
                LoadingState.Idle -> maxWidth
                    .coerceIn(ButtonDefaults.MinWidth, 320.dp)
                else -> 40.dp
            },
            animationSpec = tween(500),
            "btn width animation")

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when (loadingButtonState.loadingState) {
                LoadingState.Loading -> {
                    Box(
                        modifier = Modifier
                            .width(widthAnimationState.value)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        if (widthAnimationState.value == 40.dp) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                strokeWidth = 3.dp,
                                trackColor = MaterialTheme.colorScheme.primary,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp))
                        }
                    }
                }

                LoadingState.Success -> {
                    Icon(
                        imageVector = ShopsSellerIcons.Check,
                        contentDescription = "Success",
                        tint = loadingButtonState.loadingState.foreGroundColor,
                        modifier = Modifier
                            .width(widthAnimationState.value)
                            .height(40.dp)
                            .background(
                                color = loadingButtonState.loadingState.backgroundColor,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                }

                LoadingState.Failed -> {
                    Icon(
                        imageVector = ShopsSellerIcons.Close,
                        contentDescription = "Failed",
                        tint = loadingButtonState.loadingState.foreGroundColor,
                        modifier = Modifier
                            .width(widthAnimationState.value)
                            .height(40.dp)
                            .shake(shakeController)
                            .background(
                                color = loadingButtonState.loadingState.backgroundColor,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                    shakeController.shake(
                        ShakeConfig(
                            3,
                            translateX = 10F
                        )
                    )
                }

                else -> {
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = loadingButtonState.loadingState.backgroundColor,
                        contentColor = loadingButtonState.loadingState.foreGroundColor
                    )
                    Button(
                        modifier = Modifier
                            .width(widthAnimationState.value)
                            .height(40.dp),
                        shape = RoundedCornerShape(cornerSizeMedium),
                        enabled = loadingButtonState.enabled,
                        onClick = onClick,
                        colors = buttonColors,
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }

    /* Reset the animation */
    LaunchedEffect(key1 = loadingButtonState.loadingState) {
        if (!shouldIgnoreReset) {
            /* Haptics */
            when (loadingButtonState.loadingState) {
                LoadingState.Success -> HapticUtil.createOneShot(context)
                LoadingState.Failed -> HapticUtil.createError(context)
                else -> {  /* Noop */  }
            }

            if (loadingButtonState.loadingState != LoadingState.Idle &&
                loadingButtonState.loadingState != LoadingState.Loading
            ) {
                delay(resetDelay)
                onResetRequest(loadingButtonState.loadingState)
            }
            shouldIgnoreReset = true
        } else {
            shouldIgnoreReset = false
        }
    }
}

@Composable
@DevicePreviews
private fun LoadingButtonPreview() {
    BanterboxTheme {
        ShopsBackground {
            val loadingState by remember {
                mutableStateOf(
                    LoadingButtonState(
                        initialValue = LoadingState.Idle,
                    )
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(insetLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoadingButton(
                    modifier = Modifier.fillMaxWidth()
                        .widthIn(max = 230.dp),
                    loadingButtonState = loadingState,
                    text = "Next",
                    resetDelay = 1_000L,
                    onClick = {
                        if (loadingState.loadingState == LoadingState.Loading) {
                            loadingState.loadingState = LoadingState.Idle
                        } else {
                            loadingState.loadingState = LoadingState.Loading
                        }
                    }
                )

                Spacer(modifier = Modifier.height(60.dp))

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        loadingState.loadingState = LoadingState.Loading
                    }) {
                        Text(text = "Loading")
                    }
                    Button(onClick = {
                        loadingState.loadingState = LoadingState.Success
                    }) {
                        Text(text = "Success")
                    }
                    Button(onClick = {
                        loadingState.loadingState = LoadingState.Failed
                    }) {
                        Text(text = "Failed")
                    }
                    Button(onClick = {
                        loadingState.loadingState = LoadingState.Idle
                    }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}