package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import space.banterbox.app.R
import space.banterbox.app.common.util.HapticUtil
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.Util
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.LoadingState
import space.banterbox.app.core.designsystem.exposeBounds
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.feature.onboard.presentation.components.OnboardStep
import space.banterbox.app.feature.onboard.presentation.components.OnboardStepHeader
import space.banterbox.app.showToast
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.defaultCornerSize
import space.banterbox.app.ui.defaultSpacerSize
import space.banterbox.app.ui.insetLarge
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary
import timber.log.Timber

@Composable
internal fun LaunchStoreRoute(
    viewModel: LaunchStoreViewModel = hiltViewModel(),
    onNextPage: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchStoreScreen(
        uiState = uiState,
        onLaunch = { viewModel.launchStore() },
        onConfettiComplete = {
            Timber.d("Store Launched ${System.currentTimeMillis()}")
            onNextPage()
        }
    )
}

@Composable
private fun LaunchStoreScreen(
    modifier: Modifier = Modifier,
    uiState: LaunchStoreUiState = LaunchStoreUiState(),
    onLaunch: () -> Unit = {},
    onConfettiComplete: () -> Unit = {},
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .imePadding()
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardStepHeader(currentStep = OnboardStep.LaunchStore)

            Spacer(modifier = Modifier.weight(.1F))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1F),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(1.dp))
                Box {
                    Column {
                        StoreDecoration(
                            provideStoreName = { "Sarathas Textile & Clothing" }
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                    }

                    Image(
                        painter = painterResource(id = R.drawable.ribbon),
                        contentDescription = "Ribbon",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Launch Now 🚀",
                        style = MaterialTheme.typography.titleLarge
                            .copy(fontWeight = FontWeight.W600, letterSpacing = 0.15.sp),
                    )

                    Text(
                        text = "Your store is now created and ready for Launch. Launch Now and share with Friends and Family",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = insetVeryLarge)
                    )
                }

                Spacer(modifier = Modifier.weight(1F))

                uiState.errorMessage?.let { error ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Spacer(modifier = Modifier.width(20.dp))

                        val message = error.message?.asString(LocalContext.current)
                            ?: "Something is not working"
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    LaunchedEffect(key1 = Unit) { HapticUtil.createError(context) }
                }

                if (!uiState.isLaunched) {
                    val enabled = uiState.loadState.action !is LoadState.Loading
                    Footer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(insetLarge),
                        loadState = uiState.loadState.action,
                        onClick = onLaunch,
                        enabledProvider = { enabled },
                    )
                }
            }
        }

        /*if (uiState.isLaunched) {
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    R.raw.balloons
                )
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                isPlaying = uiState.isLaunched,
                clipSpec = LottieClipSpec.Progress(0.3F, 1F),
                speed = 1.5F,
                iterations = LottieConstants.IterateForever
            )
            LottieAnimation(
                composition = composition,
                progress = { progress },
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1F),
            )
            if (progress == 1.0F) {
                onConfettiComplete()
            }
            LaunchedEffect(key1 = Unit) {
                context.showToast("Congratulations! Your store is launched.")
            }
        }*/
    }
}

@Composable
private fun StoreDecoration(
    modifier: Modifier = Modifier,
    provideStoreName: () -> String = { "" },
    onRequestFocus: () -> Unit = {},
) {
    var grassSize: Size by remember {
        mutableStateOf(Size(0f, 0f))
    }
    var storeSize: Size by remember {
        mutableStateOf(Size(0f, 0f))
    }

    BoxWithConstraints {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Max),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.store_name_decor_bg_1),
                    contentDescription = provideStoreName(),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRequestFocus)
                        .widthIn(min = 200.dp)
                        .heightIn(min = 42.dp)
                )
                Text(
                    text = provideStoreName(),
                    style = MaterialTheme.typography.titleMedium
                        .copy(fontWeight = FontWeight.W600),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(0.95F)
                        .padding(
                            vertical = insetVerySmall,
                            horizontal = insetSmall,
                        )
                        .width(IntrinsicSize.Max)
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.store_decor_bg_1),
                    contentDescription = "Grass",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            grassSize = this.size
                        }
                )
                Image(
                    painter = painterResource(id = R.drawable.store_decor_bg_2),
                    contentDescription = "Store",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth(0.75F)
                        .heightIn(min = 160.dp)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            storeSize = this.size
                        }
                )
            }
        }
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    loadState: LoadState = LoadStates.IDLE.action,
    onClick: () -> Unit = {},
    enabledProvider: () -> Boolean = { true },
) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(min = 230.dp),
        contentAlignment = Alignment.Center,
    ) {
        val widthAnimationState by animateDpAsState(
            targetValue = when (loadState) {
                is LoadState.Loading -> 40.dp
                else -> this.maxWidth
            },
            tween(500),
            "Footer width animation"
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (loadState) {
                is LoadState.Loading -> {
                    Box(
                        modifier = Modifier
                            .width(widthAnimationState)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        if (widthAnimationState == 40.dp) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                strokeWidth = 3.dp,
                                trackColor = MaterialTheme.colorScheme.primary,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            )
                        }
                    }
                }

                else -> {
                    // Idle
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(defaultCornerSize),
                        enabled = enabledProvider(),
                        modifier = Modifier
                            .width(widthAnimationState)
                            .height(40.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "Launch")
                            Spacer(modifier = Modifier.width(10.dp))
                            val composition by rememberLottieComposition(
                                spec = LottieCompositionSpec.RawRes(
                                    R.raw.onboard_launch_store
                                )
                            )
                            val progress by animateLottieCompositionAsState(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                            )
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(3F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun LaunchStoreScreenPreview() {
    BanterboxTheme {
        var uiState by remember {
            mutableStateOf(LaunchStoreUiState())
        }

        LaunchStoreScreen(
            uiState = LaunchStoreUiState(
                errorMessage = ErrorMessage(
                    0, NoInternetException(), UiText.noInternet,
                )
            ),
            onLaunch = {
                uiState = uiState.copy(isLaunched = true)
            }
        )
    }
}