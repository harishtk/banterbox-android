package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
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
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.feature.onboard.presentation.components.OnboardStep
import space.banterbox.app.feature.onboard.presentation.components.OnboardStepHeader
import space.banterbox.app.feature.onboard.presentation.components.forms.AccountNameState
import space.banterbox.app.feature.onboard.presentation.components.forms.AccountNameStateSaver
import space.banterbox.app.feature.onboard.presentation.components.forms.AccountNumberState
import space.banterbox.app.feature.onboard.presentation.components.forms.AccountNumberStateSaver
import space.banterbox.app.feature.onboard.presentation.components.forms.IfscState
import space.banterbox.app.feature.onboard.presentation.components.forms.IfscStateSaver
import space.banterbox.app.showToast
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.defaultSpacerSize
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.spacerSizeTiny
import space.banterbox.app.ui.theme.LightGray100
import space.banterbox.app.ui.theme.LightGray200
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary

private const val AccountNumberLength = 20
private const val AccountNameLength = 30
private const val AccountIfscLength = 11

@Composable
internal fun AddBankRoute(
    viewModel: AddBankViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.addBankUiState.collectAsStateWithLifecycle()

    AddBankScreen(
        uiState = uiState,
        uiAction = viewModel.accept
    )

    LaunchedEffect(key1 = uiState) {
        if (uiState == AddBankUiState.BankAdded) {
            context.showToast("Your bank is added!")
            viewModel.accept(AddBankUiAction.Reset)
            onNextPage()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddBankScreen(
    modifier: Modifier = Modifier,
    uiState: AddBankUiState,
    uiAction: (AddBankUiAction) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
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
                    .verticalScroll(rememberScrollState()),
            ) {
                OnboardStepHeader(
                    currentStep = OnboardStep.BankDetail,
                )

                when (uiState) {
                    AddBankUiState.TestingPurchase -> {
                        TestingPurchaseLayout(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    is AddBankUiState.BankDetailForm -> {
                        BandDetailFormLayout(uiState = uiState, uiAction = uiAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.BandDetailFormLayout(
    uiState: AddBankUiState.BankDetailForm,
    uiAction: (AddBankUiAction) -> Unit,
) {
    val accountNumberFocusRequester = remember { FocusRequester() }
    val accountNameFocusRequester = remember { FocusRequester() }
    val ifscNumberFocusRequester = remember { FocusRequester() }

    val accountNumberState by rememberSaveable(stateSaver = AccountNumberStateSaver) {
        mutableStateOf(AccountNumberState(uiState.accountNumber))
    }
    val accountNameState by rememberSaveable(stateSaver = AccountNameStateSaver) {
        mutableStateOf(AccountNameState(uiState.accountHolderName))
    }
    val ifscNumberState by rememberSaveable(stateSaver = IfscStateSaver) {
        mutableStateOf(IfscState(uiState.ifscNumber))
    }

    Text(
        text = "Let's Add Your Bank Account",
        style = MaterialTheme.typography.titleLarge
            .copy(fontWeight = FontWeight.W600),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = insetSmall, start = insetSmall, end = insetSmall)
    )
    Text(
        text = "Updating your Bank Account helps you collect your money easily and efficiently",
        style = MaterialTheme.typography.bodyMedium
            .copy(lineHeight = 22.sp),
        textAlign = TextAlign.Center,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = insetVeryLarge)
    )

    Spacer(modifier = Modifier.height(spacerSizeTiny))

    // TODO: Add bank details form
    AccountNumberInput(
        accountNumberState = accountNumberState,
        provideFocusRequester = { accountNumberFocusRequester }
    )
    AccountNameInput(
        accountNameState = accountNameState,
        provideFocusRequester = { accountNameFocusRequester }
    )
    IfscInput(
        ifscNumberState = ifscNumberState,
        provideFocusRequester = { ifscNumberFocusRequester }
    )

    Spacer(modifier = Modifier.weight(1F))

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
            if (!accountNumberState.isValid) {
                accountNumberState.enableShowErrors()
                isValid = false
                accountNumberFocusRequester.requestFocus()
                shouldRequestFocus = false
            }
            if (!accountNameState.isValid) {
                accountNameState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    accountNameFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }
            if (!ifscNumberState.isValid) {
                ifscNumberState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    ifscNumberFocusRequester.requestFocus()
                }
            }

            if (isValid) {
                uiAction(
                    AddBankUiAction.Submit(
                        accountNumber = accountNumberState.text,
                        accountHolderName = accountNameState.text,
                        ifscNumber = ifscNumberState.text,
                    )
                )
            }
        },
        modifier = Modifier.height(IntrinsicSize.Min)
    )
}

@Composable
private fun AccountNumberInput(
    modifier: Modifier = Modifier,
    accountNumberState: TextFieldState,
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
                append("Account Number")
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


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = accountNumberState.text,
            onValueChange = { text ->
                accountNumberState.text = text.take(AccountNumberLength)
                onValueChange(accountNumberState.text)
            },
            placeholder = {
                Text(
                    text = "Enter your account number",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
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
            singleLine = true,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = accountNumberState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .padding(vertical = insetVerySmall)
                .onFocusChanged { focusState ->
                    accountNumberState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        accountNumberState.enableShowErrors()
                    }
                },
        )

        accountNumberState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun AccountNameInput(
    modifier: Modifier = Modifier,
    accountNameState: TextFieldState,
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
                append("Account Holder Name")
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


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = accountNameState.text,
            onValueChange = { text ->
                accountNameState.text = text.take(AccountNameLength)
                onValueChange(accountNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter account holder name",
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
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = accountNameState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    accountNameState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        accountNameState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        accountNameState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun IfscInput(
    modifier: Modifier = Modifier,
    ifscNumberState: TextFieldState,
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
                append("IFSC Number")
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


        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGray100,
            unfocusedContainerColor = LightGray100,
            disabledContainerColor = LightGray100,
            focusedBorderColor = LightGray200,
            unfocusedBorderColor = LightGray200,
        )
        OutlinedTextField(
            value = ifscNumberState.text,
            onValueChange = { text ->
                ifscNumberState.text = text.take(AccountIfscLength)
                onValueChange(ifscNumberState.text)
            },
            placeholder = {
                Text(
                    text = "Enter your IFSC Number",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
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
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = ifscNumberState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    ifscNumberState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        ifscNumberState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        ifscNumberState.getError()?.let { error ->
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
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(cornerSizeMedium),
            enabled = enabled,
            onClick = onClick,
        ) {
            Text(
                text = "Test Purchase",
                style = MaterialTheme.typography.labelLarge
                    .copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
    // block:end:button
}

@Composable
private fun TestingPurchaseLayout(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Testing Your Bank Account",
            style = MaterialTheme.typography.titleLarge
                .copy(fontWeight = FontWeight.W600),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = insetSmall, start = insetSmall, end = insetSmall)
        )

        Text(
            text = "This should take less time. Thanks for your patience.",
            style = MaterialTheme.typography.bodyMedium
                .copy(lineHeight = 22.sp),
            textAlign = TextAlign.Center,
            color = TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = insetVeryLarge)
        )

        Spacer(modifier = Modifier.height(defaultSpacerSize))

        val composition by rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(
                R.raw.papersad
            )
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.6F)
                .aspectRatio(1F)
        )
    }
}

@Composable
@Preview
private fun AddBankScreenPreview() {
    BanterboxTheme {
        val form = AddBankUiState.BankDetailForm("", "", "")
        val loading = AddBankUiState.TestingPurchase

        AddBankScreen(uiState = form)
    }
}