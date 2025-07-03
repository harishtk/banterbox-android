package space.banterbox.app.feature.onboard.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import space.banterbox.app.R
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.ShopsBackground
import space.banterbox.app.core.designsystem.component.ThemePreviews
import space.banterbox.app.core.designsystem.exposeBounds
import space.banterbox.app.feature.onboard.navigation.onboardSuccessNavigationRoute
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.insetLarge
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.theme.BanterboxTheme

enum class OnboardStep(
    val step: Int,
    val title: Int,
    val lottieRes: Int,
) {
    Store(
        step = 0,
        title = R.string.set_up_your_store,
        lottieRes = R.raw.onboard_setup_store,
    ),
    Product(
        step = 1,
        title = R.string.add_products,
        lottieRes = R.raw.onboard_add_product
    ),
    BankDetail(
        step = 2,
        title = R.string.add_bank_details,
        lottieRes = R.raw.onboard_add_bank,
    ),
    LaunchStore(
        step = 3,
        title = R.string.launch,
        lottieRes = R.raw.onboard_launch_store
    ),
}

enum class OnboardStepProgress {
    None, Processing, Completed;

    val backgroundColor: Color @Composable get() =
        when (this) {
            Completed -> MaterialTheme.colorScheme.primary
            else -> Color(0xFF868686)
        }

    val foregroundColor: Color @Composable get() =
        when (this) {
            None -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25F)
            else -> MaterialTheme.colorScheme.primary
        }
}

private fun OnboardStep.getProgress(other: OnboardStep): OnboardStepProgress {
    return when {
        step < other.step -> OnboardStepProgress.None
        step > other.step -> OnboardStepProgress.Completed
        else -> OnboardStepProgress.Processing
    }
}

@Composable
fun OnboardStepHeader(
    modifier: Modifier = Modifier,
    currentStep: OnboardStep = OnboardStep.Store,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(min = 200.dp, max = 400.dp)
    ) {
        val remainingSteps = OnboardStep.values().size - (currentStep.step)
        Text(
            text = "You are $remainingSteps steps away to Create your Online Store",
            style = MaterialTheme.typography.titleMedium
                .copy(fontWeight = FontWeight.W700),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(insetMedium)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                /**
                 * 0.08F error correction is required to compensate start and end padding
                 * 0.04F each.
                 * Works on phones only!
                 */
                val stepProgress: Float =
                    (currentStep.step.plus(0F) / OnboardStep.values().size)
                    .coerceIn(0F, 1F) + (currentStep.step * 0.08F)
                val animatedProgress by animateFloatAsState(
                    targetValue = stepProgress,
                    label = "Onboard step animator",
                    animationSpec = tween(2000)
                )

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 17.dp, start = 30.dp, end = 30.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    OnboardStep.values().forEachIndexed { _, onboardStep ->
                        Column(
                            modifier = Modifier
                                .weight(1F),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val onboardStepProgress = currentStep.getProgress(onboardStep)
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .background(
                                            color = onboardStepProgress.backgroundColor,
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            shape = CircleShape,
                                            color = onboardStepProgress.foregroundColor
                                        )
                                        .aspectRatio(1F)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (onboardStepProgress) {
                                        OnboardStepProgress.Processing -> {
                                            val composition by rememberLottieComposition(
                                                spec = LottieCompositionSpec.RawRes(
                                                    onboardStep.lottieRes
                                                )
                                            )
                                            val progress by animateLottieCompositionAsState(
                                                composition = composition,
                                                isPlaying = currentStep.step == onboardStep.step,
                                                iterations = LottieConstants.IterateForever,
                                                clipSpec = if (currentStep.step > onboardStep.step) {
                                                    LottieClipSpec.Progress(1F, 1F)
                                                } else {
                                                    LottieClipSpec.Progress(0F, 1F)
                                                }
                                            )
                                            LottieAnimation(
                                                composition = composition,
                                                progress = { progress },
                                            )
                                        }
                                        OnboardStepProgress.Completed -> {
                                            Icon(
                                                imageVector = ShopsSellerIcons.Check,
                                                contentDescription = "Done",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        else -> Box(modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }

                            Text(
                                text = stringResource(id = onboardStep.title),
                                style = MaterialTheme.typography.labelSmall
                                    .copy(letterSpacing = 0.15.sp, fontWeight = FontWeight.W600),
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = insetSmall, vertical = insetVerySmall)
                                    .width(IntrinsicSize.Max)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(heightDp = 200, showBackground = true)
/*@ThemePreviews*/
/*@DevicePreviews*/
private fun OnboardStepHeaderPreview() {
    BanterboxTheme {
        ShopsBackground {
            OnboardStepHeader(
                currentStep = OnboardStep.BankDetail
            )
        }
    }
}