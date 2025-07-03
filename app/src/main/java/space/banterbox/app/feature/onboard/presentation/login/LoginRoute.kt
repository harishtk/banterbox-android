@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package space.banterbox.app.feature.onboard.presentation.login

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.banterbox.app.Constant
import space.banterbox.app.ObserverAsEvents
import space.banterbox.app.R
import space.banterbox.app.common.util.HapticUtil
import space.banterbox.app.common.util.ResolvableException
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.VerticalDivider
import space.banterbox.app.core.designsystem.autoFill
import space.banterbox.app.core.designsystem.component.LoadingButton
import space.banterbox.app.core.designsystem.component.LoadingButtonState
import space.banterbox.app.core.designsystem.component.LoadingState
import space.banterbox.app.core.designsystem.component.ShopsBackground
import space.banterbox.app.core.designsystem.component.animation.circularReveal
import space.banterbox.app.core.designsystem.component.text.CountryCodeState
import space.banterbox.app.core.designsystem.component.text.PhoneNumberState
import space.banterbox.app.core.designsystem.component.text.PhoneNumberStateSaver
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.supportFoldables
import space.banterbox.app.core.domain.model.CountryCodeModel
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.feature.onboard.OnboardSharedViewModel
import space.banterbox.app.feature.onboard.presentation.boarding.TextFieldError
import space.banterbox.app.feature.onboard.presentation.util.AccountUnavailableException
import space.banterbox.app.feature.onboard.presentation.util.InvalidMobileNumberException
import space.banterbox.app.feature.onboard.presentation.util.OtpLimitReachedException
import space.banterbox.app.feature.onboard.presentation.util.RecaptchaException
import space.banterbox.app.getEmoji
import space.banterbox.app.nullAsEmpty
import space.banterbox.app.showToast
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * TODO: 1. Deleted account recovery flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginRoute(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    onboardSharedViewModel: OnboardSharedViewModel,
    onOpenWebPage: (url: String) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiAction = viewModel.accept

    val legalErrorText = stringResource(id = R.string.read_and_accept_terms)

    val countryCodeState by remember {
        mutableStateOf(CountryCodeState(uiState.countryCodeModel.isocode))
    }
    countryCodeState.text = uiState.countryCodeModel.isocode
    val phoneNumberState by rememberSaveable(stateSaver = PhoneNumberStateSaver) {
        mutableStateOf(PhoneNumberState(countryCodeState, uiState.typedPhone))
    }

    LoginScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = uiAction,
        phoneNumberState = phoneNumberState,
        onOpenWebPage = onOpenWebPage,
        snackbarHostState = snackbarHostState,
        onValidate = {
            if (!uiState.toggleButtonState) {
                scope.launch {
                    snackbarHostState.showSnackbar(legalErrorText)
                }
                phoneNumberState.enableShowErrors()
            } else if (!PhoneNumberState.isValidPhone(
                    phoneNumberState.text,
                    uiState.countryCodeModel.isocode
                )
            ) {
                phoneNumberState.enableShowErrors()
            } else {
                uiAction(
                    LoginUiAction.Validate(
                        suppressError = false,
                        isDeletedAccountRetrieve = false
                    )
                )
            }
        },
    )

    if (uiState.isOtpSent) {
        val message = stringResource(id = R.string.otp_sent_successfully)
        LaunchedEffect(key1 = uiState.isOtpSent) {
            val phone = uiState.typedPhone
            val countryCode = uiState.countryCodeModel.dialcode
                .replace(Regex("\\W"), "")
            val accountType = uiState.accountType

            onboardSharedViewModel.setAccountData(
                phone = phone, countryCode = countryCode, accountType = accountType
            )

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            delay(500)
            viewModel.resetOtpSent()
            onNextPage()
        }
    }

    ObserverAsEvents(flow = viewModel.uiEvent) { event ->
        when (event) {
            is LoginUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }

            is LoginUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    /*LaunchedEffect(key1 = uiState.exception) {
        if (uiState.exception != null) {
            // TODO: parse exceptions?
            uiState.uiErrorMessage?.let { uiText ->
                uiAction(LoginUiAction.ErrorShown(0))
                snackBarHostState.showSnackbar(uiText.asString(context))
            }
        }
    }*/

    Timber.d("Error: ${phoneNumberState.getError()}")
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
internal fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginViewModelState = LoginViewModelState(),
    uiAction: (LoginUiAction) -> Unit = {},
    phoneNumberState: TextFieldState = PhoneNumberState(),
    onOpenWebPage: (url: String) -> Unit = {},
    onValidate: () -> Unit = { },
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val scope = rememberCoroutineScope()

    var showOtpLimitReachedDialog by remember {
        mutableStateOf(false)
    }
    var showCountryRestrictionDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .supportFoldables()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->

        val isCircularRevealVisible = remember { mutableStateOf(false) }
        val revealFrom = remember { mutableStateOf(Offset(0f, 0f)) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = insetVeryLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppBrand()
                Spacer(modifier = Modifier.height(60.dp))

                PhoneInput(
                    countryCodeModel = uiState.countryCodeModel,
                    phoneNumberState = phoneNumberState,
                    onChange = { uiAction(LoginUiAction.TypingPhone(it)) },
                    onChooseCountry = { showCountryRestrictionDialog = true; },
                    onImeAction = onValidate
                )

                Spacer(modifier = Modifier.height(insetMedium))

                val loadingButtonState by remember {
                    mutableStateOf(LoadingButtonState())
                }
                loadingButtonState.enabled = phoneNumberState.text.isNotBlank() &&
                        uiState.loadState.action !is LoadState.Loading

                loadingButtonState.loadingState = when (uiState.loadState.action) {
                    is LoadState.Loading -> LoadingState.Loading
                    is LoadState.Error -> {
                        Timber.e(uiState.loadState.action.error)
                        if (uiState.loadState.action.error is NoInternetException) {
                            val noInternetErrorText = stringResource(id = R.string.check_your_internet)
                            LaunchedEffect(key1 = Unit) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(noInternetErrorText)
                                }
                            }
                        } else {
                            val somethingWentWrongText = stringResource(id = R.string.something_went_wrong_try_later)
                            LaunchedEffect(key1 = Unit) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(somethingWentWrongText)
                                }
                            }
                        }
                        LoadingState.Failed
                    }
                    else -> {
                        if (uiState.isOtpSent) {
                            LoadingState.Success
                        } else {
                            LoadingState.Idle
                        }
                    }
                }

                var loadingButtonWidth by remember {
                    mutableIntStateOf(0)
                }
                LoadingButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            it
                                .positionInRoot()
                                .let { offset ->
                                    revealFrom.value = Offset(
                                        x = loadingButtonWidth / 2f,
                                        y = offset.y
                                    )
                                }
                        }
                        .onSizeChanged {
                            loadingButtonWidth = it.width
                        },
                    loadingButtonState = loadingButtonState,
                    text = "Get OTP",
                    onClick = onValidate,
                    resetDelay = 50 /* for haptics */,
                    onResetRequest = { currentState ->
                        // uiAction(LoginUiAction.ResetLoading)
                        if (currentState == LoadingState.Success) {
                            isCircularRevealVisible.value = true
                        }

                        scope.launch {
                            delay(500)
                            //uiAction(LoginUiAction.ResetLoading)
                            loadingButtonState.loadingState = LoadingState.Idle
                            isCircularRevealVisible.value = false
                        }
                    }
                )

                LegalLayout(
                    checked = uiState.toggleButtonState,
                    onCheckedChange = { uiAction(LoginUiAction.ToggleConsentButton(it)) },
                    onOpenWebPage = onOpenWebPage,
                )
            }

            Text(
                modifier = Modifier.padding(insetSmall),
                text = stringResource(id = R.string.made_with_from),
                style = MaterialTheme.typography.labelSmall
            )
        }

        /* Success circular reveal */
        CompositionLocalProvider(
            LocalAbsoluteTonalElevation provides 2.dp
        ) {
            Box(
                modifier = Modifier
                    .circularReveal(
                        isVisible = isCircularRevealVisible.value,
                        revealFrom = revealFrom.value
                    )
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = isCircularRevealVisible.value,
                    enter = scaleIn(
                        initialScale = 3f,
                    )
                ) {
                    Icon(
                        imageVector = ShopsSellerIcons.Check,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    )
                }
            }
        }

        if (showOtpLimitReachedDialog) {
            OtpLimitReachedDialog {
                showOtpLimitReachedDialog = false
            }
        }

        if (showCountryRestrictionDialog) {
            CountryRestrictionDialog {
                showCountryRestrictionDialog = false
            }
        }
    }

    val context = LocalContext.current

    /**
     * This type of Error handling doesn't work as expected with Compose
     *//*
    val notLoading = uiState.loadState.action !is LoadState.Loading
    val hasErrors = uiState.exception != null
    Timber.d("hasErrors = $hasErrors; notLoading = $notLoading")
    if (notLoading && hasErrors) {
        // TODo: show error
        val e = uiState.exception as? Exception
        val uiErr = uiState.uiErrorMessage
        if (e != null) {
            Timber.d(e)
            var errorMessage: String? = null
            when (e) {
                is ResolvableException -> {
                    // btnGetOtp.shakeNow()
                    HapticUtil.createError(LocalContext.current)
                    errorMessage = uiErr?.asString(context)
                }

                is ApiException -> {
                    when (e.cause) {
                        is RecaptchaException -> {
                            *//*triggerSafetyNet { recaptchaToken ->
                                viewModel.setRecaptchaToken(recaptchaToken)
                            }*//*
                        }

                        is OtpLimitReachedException -> {
                            // TODO: show otp limit reached
                            // showOtpLimitReachedAlert()
                            showOtpLimitReachedDialog = true
                            uiAction(LoginUiAction.ErrorShown(0))
                        }

                        is InvalidMobileNumberException -> {
                            // btnGetOtp.shakeNow()
                            HapticUtil.createError(LocalContext.current)
                            errorMessage = stringResource(id = R.string.please_check_your_number)
                        }

                        is AccountUnavailableException -> {
                            // showDeletedAccountAlert()
                        }
                    }
                }

                else -> {
                    // uiErr?.let { root.showSnack(uiErr.asString(requireContext())) }
                    errorMessage = uiErr?.asString(context)
                }
            }
            Timber.d("Error: message=${errorMessage}")
            errorMessage?.let { message ->
                scope.launch {
                    uiAction(LoginUiAction.ErrorShown(0))
                    snackbarHostState.showSnackbar(message)
                    // context.showToast(message)
                }
            }
        }
    }*/

    val notLoading = uiState.loadState.action !is LoadState.Loading
    val hasErrors = uiState.exception != null

    if (notLoading && hasErrors) {
        uiState.exception?.let { e ->
            when (e) {
                is ResolvableException -> {
                    // btnGetOtp.shakeNow()
                    HapticUtil.createError(LocalContext.current)
                }

                is ApiException -> {
                    when (e.cause) {
                        is RecaptchaException -> {

                        }

                        is OtpLimitReachedException -> {
                            // TODO: show otp limit reached
                            // showOtpLimitReachedAlert()
                            showOtpLimitReachedDialog = true
                            uiAction(LoginUiAction.ErrorShown(0))
                        }

                        is InvalidMobileNumberException -> {
                            // btnGetOtp.shakeNow()
                            HapticUtil.createError(LocalContext.current)
                            val errorMessage =
                                stringResource(id = R.string.please_check_your_number)
                        }

                        is AccountUnavailableException -> {
                            // showDeletedAccountAlert()
                        }
                    }
                }

                else -> {
                    // uiErr?.let { root.showSnack(uiErr.asString(requireContext())) }
                }
            }
            uiAction(LoginUiAction.ErrorShown(0))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PhoneInput(
    phoneNumberState: TextFieldState,
    countryCodeModel: CountryCodeModel,
    onChange: (typed: String) -> Unit,
    onChooseCountry: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(horizontal = insetSmall)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mergedTextStyle = MaterialTheme.typography
                .bodyMedium

            val countryFlag = run {
                var flagText = ""
                countryCodeModel.unicode.forEach {
                    flagText += getEmoji(Integer.parseInt(it.substring(2), 16))
                }
                flagText
            }
            val countryDialCode = countryCodeModel.dialcode

            Row(modifier = Modifier
                .clickable { onChooseCountry() }) {
                Text(
                    text = countryFlag,
                    style = mergedTextStyle
                )
                Icon(
                    modifier = Modifier.rotate(90f),
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Expand country codes"
                )
            }

            Text(
                text = countryDialCode,
                style = mergedTextStyle
            )

            VerticalDivider(
                Modifier
                    .padding(horizontal = insetVerySmall, vertical = insetSmall),
                color = Color.LightGray,
            )

            val interactionSource = remember {
                MutableInteractionSource()
            }

            val enabled = true
            var wasAutoFilled by remember { mutableStateOf(false) }

            BasicTextField(
                value = phoneNumberState.text,
                onValueChange = {
                    phoneNumberState.text = it.take(15)
                    onChange(phoneNumberState.text)
                    wasAutoFilled = false
                },
                modifier = Modifier
                    .autoFill(
                        autofillTypes = listOf(AutofillType.PhoneNumberNational),
                        onFill = {
                            phoneNumberState.text = it.take(15)
                            onChange(phoneNumberState.text)
                            wasAutoFilled = true
                        }
                    )
                    .onKeyEvent { e ->
                        if (e.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                            onImeAction()
                            return@onKeyEvent true
                        }
                        false
                    }
                    .fillMaxWidth()
                    .padding(horizontal = insetMedium, vertical = insetSmall)
                    .background(
                        color = if (wasAutoFilled) Color(0xFFFFFDE7) else Color.Transparent,
                    )
                    .onFocusChanged { focusState ->
                        phoneNumberState.onFocusChange(focusState.isFocused)
                    },
                interactionSource = interactionSource,
                enabled = enabled,
                textStyle = mergedTextStyle
                    .copy(fontWeight = FontWeight.SemiBold),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = imeAction
                ),
                /*keyBoardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                keyBoardActions = KeyboardActions {
                    defaultKeyboardAction(ImeAction.Go)
                },*/
            )
        }
        Divider()

        val showError = phoneNumberState.getError() != null
        AnimatedVisibility(visible = showError) {
            Timber.d("Error: ${phoneNumberState.getError()}")
            TextFieldError(textError = phoneNumberState.getError().nullAsEmpty())
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun OtpLimitReachedDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.label_ok))
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.dear_user),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                // Divider()
            }
        },
        text = {
            Text(
                text = stringResource(id = R.string.max_otp_limit_description),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    )
}

@ExperimentalMaterial3Api
@Composable
private fun CountryRestrictionDialog(
    onDismiss: () -> Unit,
) {
    val appName = stringResource(id = R.string.app_name)
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.label_ok))
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.welcome_to__app, appName),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                // Divider()
            }
        },
        text = {
            Text(
                text = stringResource(id = R.string.country_restriction_description, appName),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    )
}

@Composable
private fun ColumnScope.AppBrand(
    modifier: Modifier = Modifier,
) {
    val targetAnimationState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = targetAnimationState,
        enter = slideInVertically(
            animationSpec = tween(
                500,
            )
        ) { fullHeight ->
            fullHeight / 3
        } + fadeIn(
            // Overwrites the default animation with tween
            animationSpec = tween(durationMillis = 200, delayMillis = 200)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.25f)
                .aspectRatio(1f)
                .padding(insetSmall)
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(1F),
                painter = painterResource(id = ShopsSellerIcons.Id_BrandFilled),
                contentDescription = "Shops Seller App",
            )
        }
    }
    Text(
        text = "Shops Seller App", style = MaterialTheme.typography.titleLarge
            .copy(fontWeight = FontWeight.W700)
    )
}

@Composable
private fun LegalLayout(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenWebPage: (url: String) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(vertical = insetMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )

        val hyperLinkText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = TextSecondary)) {
                append("I accept ")
            }

            pushStringAnnotation(
                "privacy",
                annotation = Constant.PRIVACY_POLICY_URL
            )
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(" Privacy Policy ")
            }
            pop()

            withStyle(style = SpanStyle(color = TextSecondary)) {
                append("and")
            }

            pushStringAnnotation(
                "terms",
                annotation = Constant.TERMS_URL
            )
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(" Terms & Conditions ")
            }
            pop()
        }
        ClickableText(
            modifier = Modifier.padding(0.dp),
            text = hyperLinkText,
            style = MaterialTheme.typography.bodyMedium
                .copy(fontWeight = FontWeight.W500)
        ) { offset ->
            Timber.d("Click: offset = $offset")
            hyperLinkText.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                .firstOrNull()?.let {
                Timber.d("Annotation: ${it.item}")
                onOpenWebPage(it.item)
            }
            hyperLinkText.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                Timber.d("Annotation: ${it.item}")
                onOpenWebPage(it.item)
            }
        }
    }
}

// @ThemePreviews
// @Preview(group = "main", device = Devices.PIXEL_3A, apiLevel = 33, showBackground = true)
@DevicePreviews
@Composable
private fun LoginScreenPreview() {
    BoxWithConstraints {
        BanterboxTheme {
            ShopsBackground {
                LoginScreen()
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
private fun OtpLimitDialogPreview() {
    BanterboxTheme {
        OtpLimitReachedDialog {}
    }
}

@Preview(showBackground = true)
@Composable
private fun LegalLayoutPreview() {
    var checked by remember {
        mutableStateOf(false)
    }
    BanterboxTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegalLayout(checked = checked, onCheckedChange = { checked = it }, onOpenWebPage = {})
        }
    }
}