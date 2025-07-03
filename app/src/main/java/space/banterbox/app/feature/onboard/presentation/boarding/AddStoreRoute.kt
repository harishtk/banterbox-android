package space.banterbox.app.feature.onboard.presentation.boarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.banterbox.app.ObserverAsEvents
import space.banterbox.app.R
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.EmptyState
import space.banterbox.app.core.designsystem.component.EmptyStateView
import space.banterbox.app.core.designsystem.component.LoadingButton
import space.banterbox.app.core.designsystem.component.LoadingButtonState
import space.banterbox.app.core.designsystem.component.LoadingState
import space.banterbox.app.core.designsystem.component.ShopsBackground
import space.banterbox.app.core.designsystem.component.ThemePreviews
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.feature.onboard.domain.model.ShopCategory
import space.banterbox.app.feature.onboard.presentation.components.OnboardStep
import space.banterbox.app.feature.onboard.presentation.components.OnboardStepHeader
import space.banterbox.app.feature.onboard.presentation.util.StoreCategoryState
import space.banterbox.app.feature.onboard.presentation.util.StoreNameState
import space.banterbox.app.feature.onboard.presentation.util.StoreNameStateSaver
import space.banterbox.app.nullAsEmpty
import space.banterbox.app.showToast
import space.banterbox.app.titleCase
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.insetLarge
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.insetVerySmall
import space.banterbox.app.ui.theme.LightGray100
import space.banterbox.app.ui.theme.LightGray200
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalLayoutApi
@Composable
internal fun AddStoreRoute(
    viewModel: AddStoreViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {}
) {
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()
    val addStoreUiState by viewModel.addStoreUiState.collectAsStateWithLifecycle()
    val categoriesUiState by viewModel.storeCategoriesUiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    AddStoreScreen(
        addStoreUiState = addStoreUiState,
        storeCategoriesUiState = categoriesUiState,
        uiAction = viewModel.accept,
        snackbarHostState = snackBarHostState,
    )

    LaunchedEffect(key1 = addStoreUiState.storeAdded) {
        if (addStoreUiState.storeAdded) {
            context.showToast("Your Store has been created!")
            viewModel.accept(AddStoreUiAction.Reset)
            onNextPage()
        }
    }

    ObserverAsEvents(flow = viewModel.uiEvent) { event ->
        when (event) {
            is AddStoreUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }
            is AddStoreUiEvent.ShowSnack -> {
                scope.launch {
                    snackBarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }
}

/**
 * TODO: Can we avoid recompositions by not sending the [AddStoreUiAction.TypingStoreName] Event to ViewModel?
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AddStoreScreen(
    modifier: Modifier = Modifier,
    addStoreUiState: AddStoreUiState,
    storeCategoriesUiState: StoreCategoriesUiState,
    uiAction: (AddStoreUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }

    val storeCategoryState by rememberSaveable(stateSaver = StoreNameStateSaver) {
        mutableStateOf(StoreCategoryState(addStoreUiState.storeCategory?.categoryName.nullAsEmpty()))
    }

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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val storeNameFocusRequester = remember { FocusRequester() }
            val storeCategoryFocusRequester = remember { FocusRequester() }
            val storeNameState by rememberSaveable(stateSaver = StoreNameStateSaver) {
                mutableStateOf(StoreNameState(addStoreUiState.storeName))
            }
            if (addStoreUiState.storeAdded) {
                storeNameState.text = ""
                storeCategoryState.text = ""
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, false)
                    .verticalScroll(rememberScrollState())
            ) {
                OnboardStepHeader(
                    currentStep = OnboardStep.Store,
                )
                Text(
                    text = "Set up Your Online Store",
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontWeight = FontWeight.W700),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(insetSmall)
                )
                Text(
                    text = "Your online store represents your physical store and your brand",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = insetVeryLarge)
                )

                Spacer(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .heightIn(min = 20.dp)
                )
                StoreDecoration(
                    modifier = Modifier.padding(horizontal = insetMedium),
                    provideStoreName = { storeNameState.text },
                    onRequestFocus = { storeNameFocusRequester.requestFocus() }
                )
                Spacer(modifier = Modifier.weight(1f))

                StoreNameInput(
                    storeNameState = storeNameState,
                    provideFocusRequester = { storeNameFocusRequester },
                    onValueChange = { text ->
                        uiAction(AddStoreUiAction.TypingStoreName(text))
                    },
                    onImeActionNext = {
                        if (addStoreUiState.storeCategory == null) {
                            showBottomSheet = true
                        }
                    }
                )

                StoreCategoryInput(
                    storeCategoryState = storeCategoryState,
                    onDropDownClick = {
                        showBottomSheet = true
                    },
                    provideFocusRequester = { storeCategoryFocusRequester }
                )
            }

            val loadingButtonState by remember {
                mutableStateOf(LoadingButtonState())
            }
            loadingButtonState.loadingState = when (addStoreUiState.loadState.action) {
                is LoadState.Loading -> LoadingState.Loading
                is LoadState.Error -> {
                    if (addStoreUiState.loadState.action.error is NoInternetException) {
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
                    if (addStoreUiState.storeAdded) {
                        LoadingState.Success
                    } else {
                        LoadingState.Idle
                    }
                }
            }

            loadingButtonState.enabled = addStoreUiState.loadState.action !is LoadState.Loading &&
                    storeNameState.isValid && storeCategoryState.isValid

            LoadingButton(
                modifier = Modifier.padding(insetMedium),
                loadingButtonState = loadingButtonState,
                text = "Next",
                onClick = {
                    // Validation code is shit.
                    var shouldRequestFocus = true
                    var showToast = true
                    if (!storeNameState.isValid) {
                        storeNameState.enableShowErrors()
                        if (shouldRequestFocus) {
                            storeNameFocusRequester.requestFocus()
                            shouldRequestFocus = false
                        }
                        if (showToast) {
                            scope.launch {
                                snackbarHostState
                                    .showSnackbar("Please enter a valid name")
                            }
                            showToast = false
                        }
                    }
                    if (!storeCategoryState.isValid) {
                        storeCategoryState.enableShowErrors()
                        if (shouldRequestFocus) {
                            storeCategoryFocusRequester.requestFocus()
                            shouldRequestFocus = false
                        }
                        if (showToast) {
                            scope.launch {
                                snackbarHostState
                                    .showSnackbar("Please select a category")
                            }
                            showToast = false
                        }
                    }
                    Timber.d("name=$storeNameState category=$storeCategoryState")
                    if (storeNameState.isValid && storeCategoryState.isValid) {
                        uiAction(AddStoreUiAction.Submit(storeNameState.text, storeCategoryState.text))
                    }
                },
                onResetRequest = {
                    uiAction(AddStoreUiAction.ResetLoading)
                }
            )
        }

        // TODO: show category bottom sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                sheetState = sheetState,
                onDismissRequest = {
                    showBottomSheet = false
                }
            ) {
                StoreCategoryPicker(
                    uiState = storeCategoriesUiState,
                    provideSheetState = { sheetState },
                    onDismiss = {
                        showBottomSheet = false
                    },
                    onValue = { shopCategory ->
                        uiAction(AddStoreUiAction.ToggleSelectedCategory(shopCategory))
                        storeCategoryState.text = shopCategory.categoryName
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    },
                    onRetry = {
                        uiAction(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun StoreNameInput(
    modifier: Modifier = Modifier,
    storeNameState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
    onImeActionNext: () -> Unit = {},
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
                append("Your store name")
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
            value = storeNameState.text,
            onValueChange = { text ->
                storeNameState.text = text.take(30)
                onValueChange(storeNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter your store name",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                    onImeActionNext()
                }
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
            isError = storeNameState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    storeNameState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        storeNameState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        storeNameState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Deprecated("unused")
@Composable
private fun StoreCategoryInput(
    modifier: Modifier = Modifier,
    provideCategoryName: () -> String,
    onDropDownClick: () -> Unit = {},
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium
    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Store Category")
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

        val interactionSource = remember { MutableInteractionSource() }

        OutlinedTextField(
            value = provideCategoryName(),
            onValueChange = { _ ->

            },
            readOnly = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = "Enter your store category",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            suffix = {
                Icon(
                    imageVector = ShopsSellerIcons.ChevronRight,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(90f)
                )
            },
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = insetVerySmall),
        )
        // TODO: add error supporting text?

        val pressedState = interactionSource.interactions.collectAsState(
            initial = PressInteraction.Cancel(PressInteraction.Press(Offset.Zero))
        )
        if (pressedState.value is PressInteraction.Release) {
            onDropDownClick()
            interactionSource.tryEmit(PressInteraction.Cancel(PressInteraction.Press(Offset.Zero)))
        }
    }
}

/**
 * TODO: can use regular textfield state holders instead of creating one.!!
 */
@Composable
private fun StoreCategoryInput(
    modifier: Modifier = Modifier,
    storeCategoryState: TextFieldState,
    onDropDownClick: () -> Unit = {},
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val context = LocalContext.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium
    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Store Category")
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

        val interactionSource = remember { MutableInteractionSource() }

        OutlinedTextField(
            value = storeCategoryState.text,
            onValueChange = { _ ->

            },
            readOnly = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = "Enter your store category",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            suffix = {
                Icon(
                    imageVector = ShopsSellerIcons.ChevronRight,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(90f)
                )
            },
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            colors = colors,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = storeCategoryState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = insetVerySmall)
                .onFocusChanged { focusState ->
                    storeCategoryState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        storeCategoryState.enableShowErrors()
                    }
                }
                .focusRequester(provideFocusRequester()),
        )
        // TODO: add error supporting text?
        storeCategoryState.getError()?.let { error ->
            TextFieldError(textError = error)
        }

        val pressedState = interactionSource.interactions.collectAsState(
            initial = PressInteraction.Cancel(PressInteraction.Press(Offset.Zero))
        )
        if (pressedState.value is PressInteraction.Release) {
            onDropDownClick()
            interactionSource.tryEmit(PressInteraction.Cancel(PressInteraction.Press(Offset.Zero)))
            storeCategoryState.onFocusChange(true)
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
                text = "Next",
                style = MaterialTheme.typography.labelLarge
                    .copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
    // block:end:button
}

@Composable
private fun StoreDecoration(
    modifier: Modifier = Modifier,
    provideStoreName: () -> String = { "" },
    onRequestFocus: () -> Unit = {},
) {
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
                    .widthIn(min = 150.dp)
                    .height(42.dp)
            )
            Text(
                text = provideStoreName(),
                style = MaterialTheme.typography.titleMedium
                    .copy(fontWeight = FontWeight.W600),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
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
            )
            Image(
                painter = painterResource(id = R.drawable.store_decor_bg_2),
                contentDescription = "Store",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .heightIn(min = 160.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreCategoryPicker(
    modifier: Modifier = Modifier,
    uiState: StoreCategoriesUiState = StoreCategoriesUiState.Idle,
    provideSheetState: () -> SheetState,
    onDismiss: () -> Unit = {},
    onValue: (shopCategory: ShopCategory) -> Unit = {},
    onRetry: (AddStoreUiAction.Refresh) -> Unit = {},
) {
    Timber.d("StoreCategoryPicker() called with: uiState = [$uiState]")

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        when (uiState) {
            StoreCategoriesUiState.Idle -> {}
            StoreCategoriesUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(insetLarge)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier,
                        strokeWidth = 3.dp
                    )
                    Text(text = "Loading..")
                }
            }

            is StoreCategoriesUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (uiState.exception) {
                        is NoInternetException -> {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                val emptyState = EmptyState.noInternet(LocalContext.current)
                                    .copy(
                                        showActionButton = true,
                                        actionButtonText = "Retry",
                                        onActionButtonClick = { onRetry(AddStoreUiAction.Refresh) }
                                    )
                                EmptyStateView(
                                    state = emptyState.copy()
                                )
                            }
                        }
                        else -> {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                val emptyState = EmptyState.default(LocalContext.current)
                                    .copy(
                                        title = "Oops!",
                                        description = stringResource(id = R.string.something_went_wrong_try_later),
                                        showActionButton = true,
                                        actionButtonText = "Retry",
                                        onActionButtonClick = { onRetry(AddStoreUiAction.Refresh) }
                                    )
                                EmptyStateView(
                                    state = emptyState.copy()
                                )
                            }
                        }
                    }
                }
            }

            is StoreCategoriesUiState.CategoriesList -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Select a category",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(insetSmall)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        itemsIndexed(uiState.shopCategories) { index, category ->
                            ShopCategoryRowItem(
                                modifier = Modifier,
                                shopCategory = category,
                                selected = category == uiState.selectedCategory,
                                onClick = { onValue(category) }
                            )
                            Divider(color = LightGray200)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopCategoryRowItem(
    modifier: Modifier = Modifier,
    shopCategory: ShopCategory,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = shopCategory.categoryName.titleCase(),
            modifier = Modifier
                .clickable(onClick = onClick)
                .weight(1F),
            style = if (selected) {
                mergedTextStyle.copy(fontWeight = FontWeight.W600)
            } else {
                mergedTextStyle
            },
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * To be removed when [TextField]s support error
 */
@Composable
fun TextFieldError(textError: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textError,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
@Preview(group = "main", showBackground = true)
@ThemePreviews
private fun AddStoreScreenPreview() {
    BanterboxTheme {
        ShopsBackground {
            AddStoreScreen(
                addStoreUiState = AddStoreUiState(),
                storeCategoriesUiState = StoreCategoriesUiState.Idle,
                uiAction = { uiAction -> },
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview(group = "category picker", showBackground = true)
private fun CategoryPickerListPreview() {
    BanterboxTheme {
        val sheetState = rememberModalBottomSheetState()

        val sampleCategories = listOf(
            ShopCategory(
                id = 0,
                categoryName = "Textiles & Cloting",
                createdAt = "",
            ),
            ShopCategory(
                id = 1,
                categoryName = "Food & Restaurant",
                createdAt = "",
            )
        )
        StoreCategoryPicker(
            uiState = StoreCategoriesUiState.CategoriesList(
                shopCategories = sampleCategories,
                selectedCategory = sampleCategories[0],
                isLoading = false
            ),
            provideSheetState = { sheetState },
        )
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview(group = "category picker", showBackground = true)
private fun CategoryPickerLoadingPreview() {
    BanterboxTheme {
        val sheetState = rememberModalBottomSheetState()
        StoreCategoryPicker(
            uiState = StoreCategoriesUiState.Loading,
            provideSheetState = { sheetState },
        )
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview(group = "category picker", showBackground = true)
private fun CategoryPickerErrorPreview() {
    BanterboxTheme {
        val sheetState = rememberModalBottomSheetState()

        StoreCategoryPicker(
            uiState = StoreCategoriesUiState.Error(NoInternetException()),
            provideSheetState = { sheetState },
        )
    }
}

@Composable
@Preview(group = "input", showBackground = true)
private fun StoreNameInputPreview() {
    BanterboxTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            StoreNameInput(
                modifier = Modifier.fillMaxWidth(),
                storeNameState = StoreNameState("Sarathas"),
                enableCharacterCounter = false,
            )
        }
    }
}

@Composable
@Preview(group = "input", showBackground = true)
private fun StoreCategoryInputPreview() {
    BanterboxTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            StoreCategoryInput(
                provideCategoryName = { "Textiles & Cloting" }
            )
        }
    }
}

@Composable
@Preview(group = "input", showBackground = true)
private fun FooterPreview() {
    BanterboxTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Footer()
        }
    }
}

@Composable
@Preview(group = "decor", showBackground = false)
private fun StoreNameDecorPreview() {
    BanterboxTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // StoreDecoration(storeName = "Very Long" )
        }
    }
}
