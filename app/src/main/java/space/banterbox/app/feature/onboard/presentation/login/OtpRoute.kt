package space.banterbox.app.feature.onboard.presentation.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.pepul.shops.core.analytics.Analytics
import com.pepul.shops.core.analytics.LocalAnalyticsLogger
import space.banterbox.app.R
import space.banterbox.app.UserViewModel
import space.banterbox.app.common.util.InvalidOtpException
import space.banterbox.app.common.util.ResolvableException
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.core.designsystem.ShopsTopAppBarState
import space.banterbox.app.core.designsystem.component.BestDealsTopAppBar
import space.banterbox.app.core.designsystem.component.LoadingButton
import space.banterbox.app.core.designsystem.component.LoadingButtonState
import space.banterbox.app.core.designsystem.component.LoadingState
import space.banterbox.app.core.designsystem.component.ShopsBackground
import space.banterbox.app.core.designsystem.component.text.OtpFieldState
import space.banterbox.app.core.designsystem.component.text.OtpFieldStateSaver
import space.banterbox.app.core.designsystem.component.text.OtpTextField
import space.banterbox.app.core.designsystem.supportFoldables
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.feature.onboard.OnboardSharedViewModel
import space.banterbox.app.feature.onboard.presentation.util.RecaptchaException
import space.banterbox.app.hiltActivityViewModel
import space.banterbox.app.ifDebug
import space.banterbox.app.launch
import space.banterbox.app.parcelable
import space.banterbox.app.service.SMSBroadCastReceiver
import space.banterbox.app.showToast
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Composable
internal fun OtpRoute(
    args: Bundle?,
    viewModel: OtpViewModel = hiltViewModel(),
    onboardSharedViewModel: OnboardSharedViewModel,
    userViewModel: UserViewModel = hiltActivityViewModel(),
    onNavUp: () -> Unit = {},
) {
    val context = LocalContext.current
    val analyticsLogger = LocalAnalyticsLogger.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiAction = viewModel.accept

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    val smsReceiverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                // Get the SMS message content
                val message = result.data!!.getStringExtra(SmsRetriever.SMS_RETRIEVED_ACTION) ?: ""
                val otpPattern: Pattern = Pattern.compile("(|^)\\d{6}")
                otpPattern.matcher(message).let { matcher ->
                    if (matcher.find()) {
                        val retrievedOtp = matcher.group(0)
                        ifDebug { context?.showToast("Received otp: $retrievedOtp") }
                        if (!retrievedOtp.isNullOrBlank()) {
                            viewModel.setTypedOtp(retrievedOtp)
                            analyticsLogger.logEvent(Analytics.Event.LOGIN_OTP_AUTOFILL)
                        }
                    }
                }
            } else {
                val t = IllegalStateException("Unable to retrieve SMS for OTP")
                ifDebug { Timber.w(t) }
            }
        }
    )

    OtpScreen(
        uiState = uiState,
        uiAction = uiAction,
        onNavUp = {
            confirmBackPress = true
        },
        formattedPhoneProvider = { viewModel.getFormattedPhone() }
    )

    if (confirmBackPress) {
        ConfirmBackPressDialog(
            description = "Discard OTP and go back?"
        ) { result ->
            confirmBackPress = false
            if (result == 1) {
                onNavUp()
            }
        }
    }

    if (uiState.isLoginSuccessful) {
        LaunchedEffect(key1 = uiState.isLoginSuccessful) {
            userViewModel.setShouldAutoLogin(false)
        }
    }

    val signInData by onboardSharedViewModel.signInData.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = signInData) {
        Timber.d("Settings account data $signInData")
        viewModel.setPhoneNumber(
            countryCode = signInData.countryCode,
            phoneNumber = signInData.phone
        )
        viewModel.setAccountType(signInData.accountType)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(key1 = viewModel.uiEvent) {
        viewModel.uiEvent.onEach { event ->
            when (event) {
                is OtpUiEvent.ShowToast -> {
                    context.showToast(event.message.asString(context))
                }
            }
        }
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .launch(lifecycleOwner)
    }

    /* Setup SMS Retriever for OTP */
    LaunchedEffect(key1 = 1) {
        setupOtpReceiver(
            context,
            onSuccess = { consentIntent ->
                smsReceiverLauncher.launch(consentIntent)
            }
        )
    }
    /* END - Setup SMS Retriever for OTP */
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OtpScreen(
    modifier: Modifier = Modifier,
    onNavUp: () -> Unit = {},
    uiState: OtpUiState = OtpUiState(),
    uiAction: (OtpUiAction) -> Unit = {},
    formattedPhoneProvider: () -> String = { "" },
) {
    val snackBarHostState by remember {
        mutableStateOf(SnackbarHostState())
    }
    val scope = rememberCoroutineScope()

    val otpFocusRequester = remember { FocusRequester() }

    val topAppBarState = remember {
        ShopsTopAppBarState(
            title = "",
            showNavigationIcon = true,
            onNavigationIconClick = { onNavUp() }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            BestDealsTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                state = topAppBarState
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical,
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .weight(1f)
                        .padding(
                            start = insetVeryLarge,
                            end = insetVeryLarge,
                            bottom = insetVeryLarge,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    Text(
                        text = "Please enter the OTP", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600
                    )
                    Text(
                        text = "Sent to ${formattedPhoneProvider()}",
                        modifier = Modifier
                            .padding(insetVeryLarge),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    val otpState by rememberSaveable(stateSaver = OtpFieldStateSaver) {
                        mutableStateOf(OtpFieldState(uiState.typedOtp))
                    }

                    OtpTextField(
                        otpState = otpState,
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(otpFocusRequester),
                        otpLength = 6,
                        onOtpChange = { otp ->
                            uiAction(OtpUiAction.TypingOtp(otp))
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    ResendOtpLayout(
                        enableResendOtpProvider = { uiState.enableResendOtp },
                        tickMillisProvider = { uiState.tickMillis },
                        onResendOtp = { uiAction(OtpUiAction.ResendOtp) }
                    )

                    val loadingButtonState by remember {
                        mutableStateOf(LoadingButtonState())
                    }
                    loadingButtonState.enabled = uiState.typedOtp.length == 6 &&
                            uiState.loadState.action !is LoadState.Loading

                    loadingButtonState.loadingState = when (uiState.loadState.action) {
                        is LoadState.Loading -> LoadingState.Loading
                        is LoadState.Error -> {
                            LoadingState.Failed
                        }
                        else -> {
                            if (uiState.isLoginSuccessful) {
                                LoadingState.Success
                            } else {
                                LoadingState.Idle
                            }
                        }
                    }

                    LoadingButton(
                        modifier = Modifier.fillMaxWidth()
                            .widthIn(max = 200.dp),
                        loadingButtonState = loadingButtonState,
                        text = "Submit",
                        onClick = {
                            uiAction(OtpUiAction.VerifyOtp(uiState.typedOtp))
                        },
                        onResetRequest = {
                            uiAction(OtpUiAction.ResetLoading)
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = uiState.retryCount) {
        otpFocusRequester.requestFocus()
    }

    val notLoading = uiState.loadState.action !is LoadState.Loading
    val hasErrors = uiState.exception != null

    if (notLoading && hasErrors) {
        val (e, uiErr) = uiState.exception to uiState.uiErrorMessage
        when (e) {
            is ApiException -> {
                // TODO: handle
                when (e.cause) {
                    is InvalidOtpException -> {}
                    is RecaptchaException -> {}
                    else -> {}
                }
                uiErr?.asString(LocalContext.current)?.let { message ->
                    LaunchedEffect(key1 = uiErr) {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = message
                            )
                        }
                    }
                }
            }

            is ResolvableException -> {
                uiErr?.asString(LocalContext.current)?.let { message ->
                    LaunchedEffect(key1 = uiErr) {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = message
                            )
                        }
                    }
                }
            }
        }
        uiAction(OtpUiAction.ErrorShown(0))
    }
}

@Composable
internal fun ResendOtpLayout(
    enableResendOtpProvider: () -> Boolean,
    tickMillisProvider: () -> Long,
    onResendOtp: () -> Unit
) {
    if (enableResendOtpProvider()) {
        TextButton(onClick = { onResendOtp() }) {
            Text(text = stringResource(id = R.string.resend_otp))
        }
    } else {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextSecondary)) {
                    append("Didn't receive OTP? Resend in ")
                }

                pushStringAnnotation(
                    "Otp timer",
                    annotation = "timer"
                )
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.W600,
                    ),
                ) {
                    val tickMillis = tickMillisProvider()
                    val minsUntil = TimeUnit.MILLISECONDS.toMinutes(tickMillis)
                    val secondsUntil =
                        tickMillis - (TimeUnit.MINUTES.toMillis(minsUntil))
                    val time = String.format(
                        "%02d:%02d",
                        minsUntil,
                        TimeUnit.MILLISECONDS.toSeconds(secondsUntil)
                    )
                    Timber.d("Timer: ${tickMillis}ms formatted=$time")
                    append(time)
                }
                pop()
            },
            modifier = Modifier
                .padding(insetVeryLarge),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
internal fun ConfirmBackPressDialog(
    title: String = "Alert",
    description: String = "",
    onDismiss: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss(0) },
        confirmButton = {
            TextButton(onClick = { onDismiss(1) }) {
                Text(text = stringResource(id = R.string.label_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(0) }) {
                Text(text = stringResource(id = R.string.label_cancel))
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )

}

private fun setupOtpReceiver(
    context: Context,
    onSuccess: (consentIntent: Intent) -> Unit
) {
    val client = SmsRetriever.getClient(context)
    client.startSmsUserConsent(null)
        .addOnSuccessListener { _ ->
            val receiver = SMSBroadCastReceiver()
            receiver.smsRetrieverListener = object : SMSBroadCastReceiver.OnSmsRetrieveListener {
                override fun onRetrieveSms(status: String, intent: Intent?) {
                    if (status == SMSBroadCastReceiver.SMS_RECEIVER_SUCCESS) {
                        intent?.extras?.parcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            ?.let { consentIntent ->
                                onSuccess(consentIntent)
                            }
                    }
                    context.unregisterReceiver(receiver)
                }
            }

            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            ContextCompat.registerReceiver(context, receiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
        }
        .addOnFailureListener { t ->
            ifDebug {
                Timber.w(t, "Failed to initiate sms retriever")
                context.showToast("Failed to retrieve SMS")
            }
        }
}

//@ThemePreviews
@Preview(group = "main", device = Devices.PIXEL_3A, apiLevel = 33, showBackground = true)
//@DevicePreviews
@Composable
private fun OtpScreenPreview() {
    BoxWithConstraints {
        BanterboxTheme {
            ShopsBackground {
                OtpScreen()
            }
        }
    }
}

@Preview(group = "popup", showBackground = true)
@Composable
private fun ConfirmBackPressDialogPreview() {
    BanterboxTheme {
        ConfirmBackPressDialog(
            description = "Confirm go back?",
            onDismiss = {}
        )
    }
}
