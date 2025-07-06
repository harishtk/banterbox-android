package space.banterbox.app.feature.onboard.presentation.signup

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import space.banterbox.app.R
import space.banterbox.app.core.designsystem.component.forms.UsernameFieldState
import space.banterbox.app.core.designsystem.component.forms.UsernameFieldStateSaver
import space.banterbox.app.core.designsystem.component.text.PasswordFieldState
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.exposeBounds
import space.banterbox.app.feature.onboard.OnboardSharedViewModel
import space.banterbox.app.feature.onboard.presentation.boarding.AddBankUiAction
import space.banterbox.app.feature.onboard.presentation.boarding.TextFieldError
import space.banterbox.app.feature.onboard.presentation.components.forms.BioState
import space.banterbox.app.feature.onboard.presentation.components.forms.BioStateSaver
import space.banterbox.app.feature.onboard.presentation.components.forms.ConfirmPasswordState
import space.banterbox.app.feature.onboard.presentation.components.forms.DisplayNameState
import space.banterbox.app.feature.onboard.presentation.components.forms.DisplayNameStateSaver
import space.banterbox.app.feature.onboard.presentation.login.ConfirmBackPressDialog
import space.banterbox.app.showToast
import space.banterbox.app.ui.ThemePreviews
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.defaultSpacerSize
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.spacerSizeTiny
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.LightGray100
import space.banterbox.app.ui.theme.LightGray200
import space.banterbox.app.ui.theme.TextSecondary

private const val DisplayNameLength = 30
private const val BioLength = 280
private const val PasswordLength = 30
private const val UsernameLength = 30

@Composable
internal fun SignupRoute(
    viewModel: SignupViewModel = hiltViewModel(),
    onboardSharedViewModel: OnboardSharedViewModel,
    onNextPage: () -> Unit = {},
    onOpenWebPage: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.signupUiState.collectAsStateWithLifecycle()

    val onNextPageLatest by rememberUpdatedState(onNextPage)

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    SignupScreen(
        uiState = uiState,
        uiAction = viewModel.accept,
        onLoginClick = onNextPageLatest
    )

    if (confirmBackPress) {
        ConfirmBackPressDialog(
            description = "Discard signup and go back?"
        ) { result ->
            confirmBackPress = false
            if (result == 1) {
                onNextPageLatest()
            }
        }
    }

    LaunchedEffect(key1 = uiState) {
        if (uiState == SignupUiState.SignupSuccess) {
            context.showToast("Signup success")
            // viewModel.accept(SignupUiAction.Reset)
            // onNextPage()
        }
    }
}

@Composable
private fun SignupScreen(
    modifier: Modifier = Modifier,
    uiState: SignupUiState,
    uiAction: (SignupUiAction) -> Unit,
    onLoginClick: () -> Unit = {},
) {
    val snacbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(snacbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
            ) {

                when (uiState) {
                    SignupUiState.SignupSuccess -> {
                        SignupSuccessLayout(onLoginClick = onLoginClick)
                    }

                    is SignupUiState.SignupForm -> {
                        SignupFormLayout(uiState = uiState, uiAction = uiAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SignupFormLayout(
    modifier: Modifier = Modifier,
    uiState: SignupUiState.SignupForm,
    uiAction: (SignupUiAction) -> Unit,
) {
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val displayNameFocusRequester = remember { FocusRequester() }
    val bioFocusRequester = remember { FocusRequester() }

    val usernameState by rememberSaveable(stateSaver = UsernameFieldStateSaver) {
        mutableStateOf(UsernameFieldState(""))
    }
    val passwordState by remember {
        mutableStateOf(PasswordFieldState())
    }
    val confirmPasswordState by remember {
        mutableStateOf(ConfirmPasswordState(passwordState))
    }
    val displayNameState by rememberSaveable(stateSaver = DisplayNameStateSaver) {
        mutableStateOf(DisplayNameState(uiState.displayName))
    }
    val bioState by rememberSaveable(stateSaver = BioStateSaver) {
        mutableStateOf(BioState(uiState.bio))
    }

    Spacer(modifier = Modifier.height(spacerSizeTiny))

    Text(
        text = "Let's get you signed up!",
        style = MaterialTheme.typography.titleLarge
            .copy(fontWeight = FontWeight.W600),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = insetSmall, start = insetSmall, end = insetSmall)
    )

    Text(
        text = "It's very easy, just fill these in",
        style = MaterialTheme.typography.bodyMedium
            .copy(lineHeight = 22.sp),
        textAlign = TextAlign.Center,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = insetVeryLarge)
    )

    Spacer(modifier = Modifier.height(spacerSizeTiny))

    UsernameInput(
        usernameState = usernameState,
        provideFocusRequester = { usernameFocusRequester }
    )
    PasswordInput(
        passwordState = passwordState,
        provideFocusRequester = { passwordFocusRequester }
    )
    ConfirmPasswordInput(
        passwordState = confirmPasswordState,
        provideFocusRequester = { confirmPasswordFocusRequester }
    )
    DisplayNameInput(
        displayNameState = displayNameState,
        provideFocusRequester = { displayNameFocusRequester }
    )
    BioInput(
        bioState = bioState,
        provideFocusRequester = { bioFocusRequester }
    )

    // Spacer(modifier = Modifier.weight(1f))

    uiState.errorMessage?.let { error ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(8.dp))

            val message = error.message?.asString(LocalContext.current)
                ?: "Something is not working"
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    val enableNext = true
    Footer(
        enabled = enableNext,
        onClick = {
            var isValid = true
            var shouldRequestFocus = true
            if (!usernameState.isValid) {
                usernameState.enableShowErrors()
                isValid = false
                usernameFocusRequester.requestFocus()
                shouldRequestFocus = false
            }
            if (!displayNameState.isValid) {
                displayNameState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    displayNameFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }
            if (!bioState.isValid) {
                bioState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    bioFocusRequester.requestFocus()
                }
            }
            if (!passwordState.isValid) {
                passwordState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    passwordFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }
            if (!confirmPasswordState.isValid) {
                confirmPasswordState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    confirmPasswordFocusRequester.requestFocus()
                }
            }

            if (isValid) {
                uiAction(
                    SignupUiAction.Submit(
                        username = usernameState.text,
                        password = passwordState.text,
                        displayName = displayNameState.text,
                        bio = bioState.text,
                    )
                )
            }
        },
        modifier = Modifier.height(IntrinsicSize.Min)
    )
}

@Composable
private fun UsernameInput(
    modifier: Modifier = Modifier,
    usernameState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Username")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = usernameState.text,
            onValueChange = { text ->
                usernameState.text = text.take(UsernameLength)
                onValueChange(usernameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter username",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = usernameState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    usernameState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        usernameState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        usernameState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun PasswordInput(
    modifier: Modifier = Modifier,
    passwordState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Create password")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = passwordState.text,
            onValueChange = { text ->
                passwordState.text = text.take(PasswordLength)
                onValueChange(passwordState.text)
            },
            placeholder = {
                Text(
                    text = "Enter password",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = passwordState.showErrors(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    passwordState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        passwordState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        passwordState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun ConfirmPasswordInput(
    modifier: Modifier = Modifier,
    passwordState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = passwordState.text,
            onValueChange = { text ->
                passwordState.text = text.take(PasswordLength)
                onValueChange(passwordState.text)
            },
            placeholder = {
                Text(
                    text = "Confirm password",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = passwordState.showErrors(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    passwordState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        passwordState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        passwordState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun DisplayNameInput(
    modifier: Modifier = Modifier,
    displayNameState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Display Name")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = displayNameState.text,
            onValueChange = { text ->
                displayNameState.text = text.take(DisplayNameLength)
                onValueChange(displayNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter name",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = displayNameState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    displayNameState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        displayNameState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        displayNameState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun BioInput(
    modifier: Modifier = Modifier,
    bioState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("About")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = bioState.text,
            onValueChange = { text ->
                bioState.text = text.take(BioLength)
                onValueChange(bioState.text)
            },
            placeholder = {
                Text(
                    text = "I'm so cool!",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            supportingText = {
                if (enableCharacterCounter) {
                    val count = bioState.text.length
                    Text(
                        text = "$count/$BioLength",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = bioState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    bioState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        bioState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        bioState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    // block:start:button
    Box(
        modifier = modifier
            .padding(insetMedium),
    ) {
        // TODO: replace with loading button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(cornerSizeMedium),
            enabled = enabled,
            onClick = onClick,
        ) {
            Text(
                text = "Signup",
                style = MaterialTheme.typography.labelLarge
                    .copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
    // block:end:button
}

@Composable
private fun SignupSuccessLayout(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "All done!",
            style = MaterialTheme.typography.titleLarge
                .copy(fontWeight = FontWeight.W600),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = insetSmall, start = insetSmall, end = insetSmall)
        )

        Text(
            text = "Your account is now created please login",
            style = MaterialTheme.typography.bodyMedium
                .copy(lineHeight = 22.sp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = insetVeryLarge)
        )

        Spacer(modifier = Modifier.height(defaultSpacerSize))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(cornerSizeMedium),
            enabled = true,
            onClick = onLoginClick,
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.labelLarge
                    .copy(fontWeight = FontWeight.SemiBold)
            )
        }

//        val composition by rememberLottieComposition(
//            spec = LottieCompositionSpec.RawRes(
//                R.raw.papersad
//            )
//        )
//        val progress by animateLottieCompositionAsState(
//            composition = composition,
//            isPlaying = true,
//            iterations = LottieConstants.IterateForever,
//        )
//        LottieAnimation(
//            composition = composition,
//            progress = { progress },
//            modifier = Modifier
//                .fillMaxWidth(0.6F)
//                .aspectRatio(1F)
//        )
    }
}

@Preview(showBackground = false,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
// @ThemePreviews
@Composable
private fun SignupScreenPreview() {
    BanterboxTheme(
        disableDynamicTheming = false
    ) {
        SignupScreen(
            uiState = SignupUiState.SignupForm("", "", "", ""),
            uiAction = {},
        )
    }
}



