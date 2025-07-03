@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package space.banterbox.app.feature.onboard.presentation.boarding

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.banterbox.app.BuildConfig
import space.banterbox.app.Constant
import space.banterbox.app.Constant.MIME_TYPE_JPEG
import space.banterbox.app.Constant.MIME_TYPE_VIDEO
import space.banterbox.app.ObserverAsEvents
import space.banterbox.app.R
import space.banterbox.app.common.util.StorageUtil
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.EmptyState
import space.banterbox.app.core.designsystem.component.EmptyStateView
import space.banterbox.app.core.designsystem.component.forms.AddProductForm
import space.banterbox.app.core.designsystem.component.forms.MediaType
import space.banterbox.app.core.designsystem.component.forms.SellerMediaFile
import space.banterbox.app.core.designsystem.component.forms.UploadPreviewUiModel
import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.feature.onboard.presentation.components.OnboardStep
import space.banterbox.app.feature.onboard.presentation.components.OnboardStepHeader
import space.banterbox.app.ifDebug
import space.banterbox.app.showToast
import space.banterbox.app.titleCase
import space.banterbox.app.ui.cornerSizeMedium
import space.banterbox.app.ui.insetLarge
import space.banterbox.app.ui.insetMedium
import space.banterbox.app.ui.insetSmall
import space.banterbox.app.ui.insetVeryLarge
import space.banterbox.app.ui.theme.LightGray200
import space.banterbox.app.ui.theme.BanterboxTheme
import space.banterbox.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

internal const val MAX_IMAGES_LIMIT = 5
internal const val MAX_VIDEOS_LIMIT = 1

private val storagePermissions: Array<String> = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AddProductRoute(
    viewModel: AddProductViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {},
) {
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val productUiState by viewModel.productUiState.collectAsStateWithLifecycle()
    val productCategoriesUiState by viewModel.productCategoriesUiState.collectAsStateWithLifecycle()

    var showDuplicatePhotosAlert by remember {
        mutableStateOf(false to 0)
    }
    var pickerLauncherMediaType = remember { MediaType.Unknown }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES_LIMIT)
    ) { pickedUris ->
        Timber.d("Picked Uris: $pickedUris pendingType=$pickerLauncherMediaType")
        if (pickedUris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                preProcessUris(context, viewModel, pickerLauncherMediaType, pickedUris) { duplicateCount ->
                    if (duplicateCount > 0) {
                        showDuplicatePhotosAlert = true to duplicateCount
                    }
                }
                pickerLauncherMediaType = MediaType.Unknown
            }
        }
    }

    val mediaPickerGenericLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { pickedUris ->
        Timber.d("Picked Uris Generic: $pickedUris pendingType=$pickerLauncherMediaType")
        if (pickedUris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                preProcessUris(context, viewModel, pickerLauncherMediaType, pickedUris) { duplicateCount ->
                    if (duplicateCount > 0) {
                        showDuplicatePhotosAlert = true to duplicateCount
                    }
                }
                pickerLauncherMediaType = MediaType.Unknown
            }
        }
    }

    /* <bool, bool> - (show rationale, openSettings) */
    var showStoragePermissionRationale by remember {
        mutableStateOf(false to false)
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        val deniedList: List<String> = result.filter { !it.value }.map { it.key }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                        Constant.PERMISSION_DENIED
                    } else {
                        Constant.PERMISSION_PERMANENTLY_DENIED
                    }
                }
                map[Constant.PERMISSION_DENIED]?.let {
                    // context.showToast("Storage permission is required to upload photos")
                    showStoragePermissionRationale = true to false
                }
                map[Constant.PERMISSION_PERMANENTLY_DENIED]?.let {
                    // context.showToast("Storage permission is required to upload photos")
                    showStoragePermissionRationale = true to true
                }
            }

            else -> {
                // TODO: Handle continuation?
            }
        }
    }

    AddProductScreen(
        provideSnackbarHostState = { snackbarHostState },
        productUiState = productUiState,
        productCategoriesUiState = productCategoriesUiState,
        uiAction = viewModel.accept,
        launchMediaPicker = { _, type ->
            Timber.d("Launch media picker: type=$type")
            when (type) {
                MediaType.Image -> {
                    val maxPick = viewModel.getMaxImages()
                    Timber.d("Mx pick: $maxPick")

                    if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.SingleMimeType(MIME_TYPE_JPEG))
                        )
                        pickerLauncherMediaType = type
                    } else {
                        Timber.w("No media picker available. Using generic picker.")
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (!context.checkStoragePermission()) {
                                /* mStoragePermissionContinuation = {
                                     photoPickerGenericLauncher.launch(MIME_TYPE_IMAGE)
                                 }*/
                                storagePermissionLauncher.launch(storagePermissions)
                            } else {
                                mediaPickerGenericLauncher.launch(MIME_TYPE_JPEG)
                                context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                pickerLauncherMediaType = type
                            }
                        } else {
                            mediaPickerGenericLauncher.launch(MIME_TYPE_JPEG)
                            context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                            pickerLauncherMediaType = type
                        }
                    }
                }
                MediaType.Video -> {
                    val maxPick = viewModel.getMaxVideos()
                    Timber.d("Mx pick: $maxPick")

                    Timber.w("No media picker available. Using generic picker.")
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (!context.checkStoragePermission()) {
                            /* mStoragePermissionContinuation = {
                                 photoPickerGenericLauncher.launch(MIME_TYPE_IMAGE)
                             }*/
                            storagePermissionLauncher.launch(storagePermissions)
                        } else {
                            mediaPickerGenericLauncher.launch(MIME_TYPE_VIDEO)
                            context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                            pickerLauncherMediaType = type
                        }
                    } else {
                        mediaPickerGenericLauncher.launch(MIME_TYPE_VIDEO)
                        context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                        pickerLauncherMediaType = type
                    }

                    /*if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(
                               ActivityResultContracts.PickVisualMedia.VideoOnly
                            )
                        )
                        pickerLauncherMediaType = type
                    } else {

                    }*/
                }
                MediaType.Unknown -> {
                    val t = IllegalStateException("Unable to proceed with media type '*'")
                    if (BuildConfig.DEBUG) { throw t }
                    else { Timber.w(t) }
                }
            }
        },
        onDeleteMedia = { _, type, uri ->
            viewModel.deleteMedia(type, uri)
        },
        onSkip = {
            viewModel.skipStep()
        }
    )

    LaunchedEffect(key1 = productUiState.productAdded) {
        if (productUiState.productAdded) {
            context.showToast("Your product is added!")
            viewModel.accept(AddProductUiAction.Reset)
            onNextPage()
        }
    }

    LaunchedEffect(key1 = productUiState.mediaUploaded) {
        if (productUiState.mediaUploaded) {
            context.showToast("Files uploaded successfully")
        }
    }

    ObserverAsEvents(flow = viewModel.uiEvent) { event ->
        when (event) {
            is AddProductUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }
            is AddProductUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    /* Alert dialogs */
    if (showDuplicatePhotosAlert.first) {
        DuplicateImagesDialog(showDuplicatePhotosAlert.second) {
            showDuplicatePhotosAlert = false to 0
        }
    }

    if (showStoragePermissionRationale.first) {
        StoragePermissionRationaleDialog(
            openSettings = showStoragePermissionRationale.second,
            onDismiss = { canceled ->
                if (showStoragePermissionRationale.second && !canceled) {
                    context.openSettings()
                } else if (!canceled) {
                    storagePermissionLauncher.launch(storagePermissions)
                }
                showStoragePermissionRationale = false to false
            }
        )
    }
    /* END - Alert dialogs */
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
private fun AddProductScreen(
    modifier: Modifier = Modifier,
    provideSnackbarHostState: () -> SnackbarHostState,
    productUiState: ProductUiState = ProductUiState(),
    productCategoriesUiState: ProductCategoriesUiState = ProductCategoriesUiState.Idle,
    uiAction: (AddProductUiAction) -> Unit = {},
    launchMediaPicker: (productId: Long, type: MediaType) -> Unit = { _, _ -> },
    onDeleteMedia: (productId: Long, type: MediaType, uri: Uri) -> Unit = { _, _, _ -> },
    onSkip: () -> Unit = {},
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(provideSnackbarHostState(), Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    .weight(1F)
                    .verticalScroll(rememberScrollState()),
            ) {
                OnboardStepHeader(
                    currentStep = OnboardStep.Product,
                )
                Text(
                    text = "Let's Add Your First Product",
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontWeight = FontWeight.W600),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = insetSmall, start = insetSmall, end = insetSmall)
                )
                Text(
                    text = "Your online store represents your physical store and your brand",
                    style = MaterialTheme.typography.bodyMedium
                        .copy(lineHeight = 22.sp),
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = insetVeryLarge)
                )

                AddProductForm(
                    formData = productUiState.productFormData,
                    launchMediaPicker = launchMediaPicker,
                    onDeleteMedia = onDeleteMedia
                )
            }

            val enableNext = true
            AnimatedContent(
                targetState = enableNext,
                transitionSpec = {
                                 fadeIn().togetherWith(fadeOut())
                },
                label = "Footer animation"
            ) {
                Footer(
                    enabled = it,
                    onClick = {
                        uiAction(AddProductUiAction.Submit(context))
                    },
                    onSkip = onSkip
                )
            }
        }

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
                ProductCategoryPicker(
                    provideUiState = { ProductCategoriesUiState.Idle },
                    provideSheetState = { sheetState },
                    onDismiss = {
                        showBottomSheet = false
                    },
                    onValue = { shopCategory ->
                        // TODO: toggle selected category signal to view model
                        uiAction(AddProductUiAction.ToggleSelectedCategory(0, shopCategory))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCategoryPicker(
    provideUiState: () -> ProductCategoriesUiState,
    provideSheetState: () -> SheetState,
    onDismiss: () -> Unit = {},
    onValue: (productCategory: ProductCategory) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (val uiState = provideUiState()) {
            ProductCategoriesUiState.Idle -> {}
            ProductCategoriesUiState.Loading -> {
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

            is ProductCategoriesUiState.Error -> {
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
                                        onActionButtonClick = { /* Retry */ }
                                    )
                                EmptyStateView(
                                    state = emptyState.copy()
                                )
                            }
                        }
                    }
                }
            }

            is ProductCategoriesUiState.CategoriesList -> {
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
                        itemsIndexed(uiState.productCategories) { index, category ->
                            ProductCategoryRowItem(
                                modifier = Modifier,
                                productCategory = category,
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
private fun ProductCategoryRowItem(
    modifier: Modifier = Modifier,
    productCategory: ProductCategory,
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
            text = productCategory.categoryName.titleCase(),
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

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onSkip: () -> Unit = {},
) {
    // block:start:button
    Column(
        modifier = modifier
            .padding(start = insetMedium, end = insetMedium, top = insetMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
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

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Skip")
        }
    }
    // block:end:button
}

@Composable
private fun DuplicateImagesDialog(
    count: Int = 0,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.label_ok))
            }
        },
        text = {
            Text(
                text = "$count duplicate photos have been removed.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    )
}

@Composable
private fun StoragePermissionRationaleDialog(
    openSettings: Boolean = false,
    onDismiss: (canceled: Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss(true) },
        icon = {
            Icon(
                painter = painterResource(id = ShopsSellerIcons.Id_FilePermission),
                contentDescription = "Storage Permission Required",
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth(0.2F),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss(false)
            }) {
                if (openSettings) {
                    Text(text = stringResource(id = R.string.settings))
                } else {
                    Text(text = stringResource(id = R.string.label_ok))
                }
            }
        },
        dismissButton = {
           TextButton(
               onClick = { onDismiss(true) },
               colors = ButtonDefaults.textButtonColors(
                   contentColor = TextSecondary
               )
           ) {
               Text(text = stringResource(id = R.string.label_cancel))
           }
        },
        title = {
            Text(
                text = stringResource(id = R.string.permissions_required)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.files_permission_des),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
            )
        }
    )
}

private fun Context.checkStoragePermission(): Boolean =
    storagePermissions.all {
        ContextCompat.checkSelfPermission(this, it) ==
                PackageManager.PERMISSION_GRANTED
    }

private fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri

    try {
        val resolveInfo: ResolveInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_ALL
            )
        }
        if (resolveInfo != null) {
            startActivity(intent)
        } else {
            showToast("No apps can perform this action.")
        }
    } catch (e: Exception) {
        ifDebug { Timber.e(e) }
        showToast(getString(R.string.unable_to_perform_this_action))
    }
}

private fun preProcessUris(
    context: Context,
    viewModel: AddProductViewModel,
    mediaType: MediaType,
    pickedUris: List<Uri>,
    onComplete: (duplicateCount: Int) -> Unit = {}
) {
    Timber.d("preProcessUris() called with: context = [$context], viewModel = [$viewModel], mediaType = [$mediaType], pickedUris = [$pickedUris], onComplete = [$onComplete]")
    val maxPick = if (mediaType == MediaType.Image) {
        viewModel.getMaxImages()
    } else {
        viewModel.getMaxVideos()
    }
    viewModel.removeDuplicateMedia(
        mediaType,
        pickedUris.take(maxPick)
    ) { duplicateCount, newUris ->
        Timber.d("Duplicate result: $duplicateCount $newUris")
        onComplete(duplicateCount)

        val newPreviewModelList = mutableListOf<UploadPreviewUiModel.Item>()

        val retriever = MediaMetadataRetriever()
        newUris.forEach { uri ->
            try {
                val sellerMediaFile = if (mediaType == MediaType.Video) {
                    retriever.setDataSource(context, uri)
                    val thumbnail = retriever.frameAtTime
                    val durationMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                    Timber.d("Duration: $durationMillis")
                    SellerMediaFile(
                        uri = uri,
                        mediaType = mediaType,
                        width = thumbnail?.width ?: 0,
                        height = thumbnail?.height ?: 0,
                        duration = durationMillis
                    )
                } else {
                    val imageRes = StorageUtil.getImageResolution(context, uri)
                    SellerMediaFile(
                        uri = uri,
                        mediaType = mediaType,
                        width = imageRes.width,
                        height = imageRes.height,
                    )
                }
                newPreviewModelList.add(UploadPreviewUiModel.Item(sellerMediaFile))
            } catch (ignore: Exception) { Timber.e(ignore) }
        }
        retriever.close()

        Timber.d("New uris: size = ${newPreviewModelList.size}")
        viewModel.setPickedMediaUris(
            mediaType = mediaType,
            newPreviewModelList = newPreviewModelList
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview(group = "screen", showBackground = true)
private fun AddProductScreenPreview() {
    BanterboxTheme {
        AddProductScreen(
            provideSnackbarHostState = { SnackbarHostState() },
            productUiState = ProductUiState(),
            productCategoriesUiState = ProductCategoriesUiState.Loading
        )
    }
}

@Composable
@Preview(group = "dialog")
private fun DuplicateImagesDialogPreview() {
    BanterboxTheme {
        DuplicateImagesDialog(2)
    }
}

@Composable
@Preview(group = "dialog")
private fun StoragePermissionRationaleDialogPreview() {
    BanterboxTheme {
        StoragePermissionRationaleDialog(openSettings = true)
    }
}