package space.banterbox.app.feature.onboard.presentation.boarding

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.banterbox.app.common.util.FileUtil.getMimeType
import space.banterbox.app.common.util.StorageUtil
import space.banterbox.app.common.util.StorageUtil.EXTENSION_MP4
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.Noop
import space.banterbox.app.core.data.source.local.AppDatabase
import space.banterbox.app.core.designsystem.component.forms.MediaType
import space.banterbox.app.core.designsystem.component.forms.ProductFormData
import space.banterbox.app.core.designsystem.component.forms.UploadPreviewUiModel
import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.FileUploaderCallable
import space.banterbox.app.core.util.Result
import space.banterbox.app.core.util.UnknownException
import space.banterbox.app.feature.onboard.domain.model.request.StoreCategoryRequest
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val appDatabase: AppDatabase,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState: MutableStateFlow<AddProductViewModelState> =
        MutableStateFlow(AddProductViewModelState())

    val productUiState = viewModelState
        .map(AddProductViewModelState::toProductUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toProductUiState()
        )

    val productCategoriesUiState: StateFlow<ProductCategoriesUiState> = viewModelState
        .map(AddProductViewModelState::toProductCategoriesUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toProductCategoriesUiState()
        )

    private val _uiEvent = MutableSharedFlow<AddProductUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddProductUiAction) -> Unit

    private var deletedPhotoIds: MutableList<Long> = mutableListOf()

    private var refreshCategoriesJob: Job? = null
    private var fileUploadJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        accountsRepository.productCategories("")
            .distinctUntilChanged()
            .map { categories ->
                viewModelState.update { state ->
                    state.copy(
                        productCategories = categories
                    )
                }
            }
            .launchIn(viewModelScope)
        refreshCategoriesInternal(true)

        // Initializes the place holder for media pickers.
        setPickedMediaUris(MediaType.Image, emptyList())
        setPickedMediaUris(MediaType.Video, emptyList())
    }

    private fun onUiAction(action: AddProductUiAction) {
        when (action) {
            is AddProductUiAction.Submit -> {
                // TODO: validate before files upload
                if (viewModelState.value.uploadComplete) {
                    viewModelState.update { state ->
                        state.copy(
                            isProductAddedSuccessfully = true
                        )
                    }
                } else {
                    if (fileUploadJob?.isActive == true) {
                        val message = "Already an upload is in progress!"
                        val t = IllegalStateException()
                        Timber.w(t)
                        sendEvent(AddProductUiEvent.ShowToast(UiText.DynamicString("Already an upload is in progress!")))
                    } else {
                        startUpload(action.context)
                    }
                }
            }
            AddProductUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        productCategoryLoadState = LoadStates.IDLE,
                        isProductAddedSuccessfully = false,
                    )
                }
            }
        }
    }

    fun setCachedSessionId(sessionId: Long) {
        savedStateHandle[KEY_CACHED_UPLOAD_SESSION_ID] = sessionId
    }

    fun skipStep() {
        viewModelState.update { state ->
            state.copy(
                isProductAddedSuccessfully = true,
            )
        }
        /*userDataRepository.userData.firstOrNull()?.let { userData ->
            val newLoginUser: LoginUser = userData.copy(onboardStep = "bank")
            userDataRepository.setUserData()
        }*/
    }

    private fun refreshCategoriesInternal(suppressErrors: Boolean = false) {
        if (refreshCategoriesJob?.isActive == true) {
            val t = IllegalStateException("Already a refresh job is active")
            ifDebug { Timber.w(t) }
            return
        }

        refreshCategoriesJob?.cancel(CancellationException("New request")) // just in case

        val request = StoreCategoryRequest("")

        setLoadState("store_categories", LoadType.REFRESH, LoadState.Loading())
        refreshCategoriesJob = viewModelScope.launch {
            when (val result = accountsRepository.refreshCategories(request)) {
                Result.Loading -> {}
                is Result.Error -> {
                    if (!suppressErrors) {
                        when (result.exception) {
                            is ApiException -> {
                                setLoadState(
                                    "store_categories",
                                    LoadType.REFRESH,
                                    LoadState.Error(UnknownException(result.exception))
                                )
                            }

                            is NoInternetException -> {
                                setLoadState(
                                    "store_categories",
                                    LoadType.REFRESH,
                                    LoadState.Error(UnknownException(result.exception))
                                )
                            }

                            else -> {
                                setLoadState(
                                    "store_categories",
                                    LoadType.REFRESH,
                                    LoadState.Error(UnknownException(result.exception))
                                )
                            }
                        }
                    } else {
                        val t = IllegalStateException("Error has occurred but suppressed.")
                        ifDebug { Timber.w(t) }
                    }
                }

                is Result.Success -> {
                    setLoadState("store_categories", LoadType.REFRESH, LoadState.NotLoading.Complete)
                }
            }
        }
    }

    private fun setLoadState(
        from: String = "default",
        loadType: LoadType,
        loadState: LoadState,
    ) {
        if (from == "store_categories") {
            val newLoadState = viewModelState.value.productCategoryLoadState.modifyState(loadType, loadState)
            viewModelState.update { state ->
                state.copy(
                    productCategoryLoadState = newLoadState
                )
            }
        } else {
            val newLoadState = viewModelState.value.productCategoryLoadState.modifyState(loadType, loadState)
            viewModelState.update { state ->
                state.copy(
                    loadState = newLoadState
                )
            }
        }
    }

    private fun sendEvent(newEvent: AddProductUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

    /* Upload related */
    fun getMaxImages(): Int {
        val max = MAX_IMAGES_LIMIT -
                viewModelState.value.productFormData.images.count { it !is UploadPreviewUiModel.Placeholder }
        return max.coerceAtLeast(0)
    }

    fun getMaxVideos(): Int {
        val max = MAX_VIDEOS_LIMIT -
                viewModelState.value.productFormData.videos.count { it !is UploadPreviewUiModel.Placeholder }
        return max.coerceAtLeast(0)
    }

    fun removeDuplicateMedia(
        mediaType: MediaType,
        pickedUris: List<Uri>,
        completion: (removed: Int, normalizedList: List<Uri>) -> Unit,
    ) = viewModelScope.launch(Dispatchers.Default) {
        val formData = viewModelState.value.productFormData
        val originalList = when (mediaType) {
            MediaType.Image -> {
                formData.images.filterIsInstance<UploadPreviewUiModel.Item>()
                    .map { it.sellerMediaFile.uri }
            }
            MediaType.Video -> {
                formData.videos.filterIsInstance<UploadPreviewUiModel.Item>()
                    .map { it.sellerMediaFile.uri }
            }
            else -> emptyList()
        }
        val combinedUris = originalList.toMutableList().apply {
            addAll(pickedUris)
        }

        val normalizedList = combinedUris.distinct().toMutableList()
        val removed = (combinedUris.size - normalizedList.size).coerceAtLeast(0)
        normalizedList.removeAll(originalList)
        withContext(Dispatchers.Main) {
            completion(removed, normalizedList)
        }
    }

    fun deleteMedia(mediaType: MediaType, uri: Uri) {
        Timber.d("deletePhoto: $uri type=$mediaType")
        val formData = viewModelState.value.productFormData
        val newPreviewModelList = when (mediaType) {
            MediaType.Image -> {
                formData.images
                    .filterIsInstance<UploadPreviewUiModel.Item>()
                    .filterNot { uiModel ->
                        val deleted = uiModel.sellerMediaFile.uri == uri
                        if (deleted) {
                            uiModel.sellerMediaFile.id?.let { imageId ->
                                deletedPhotoIds.add(imageId)
                            }
                        }
                        deleted
                    }
            }
            MediaType.Video -> {
                formData.videos
                    .filterIsInstance<UploadPreviewUiModel.Item>()
                    .filterNot { uiModel ->
                        val deleted = uiModel.sellerMediaFile.uri == uri
                        /*if (deleted) {
                            uiModel.sellerMediaFile.id?.let { imageId ->
                                deletedPhotoIds.add(imageId)
                            }
                        }*/
                        deleted
                    }
            }
            else -> emptyList()
        }

        setPickedMediaUris(mediaType, newPreviewModelList, append = false)
    }

    fun setPickedMediaUris(
        mediaType: MediaType,
        newPreviewModelList: List<UploadPreviewUiModel.Item>,
        append: Boolean = true,
    ) {
        // TODO: clean this sheet!
        val formData = viewModelState.value.productFormData

        when (mediaType) {
            MediaType.Image -> {
                var remainingPhotoCount = 0
                val newModelList = formData.images
                    .filterNot { it is UploadPreviewUiModel.Placeholder }
                    .toMutableList().apply {
                        if (!append) {
                            clear()
                        }
                        addAll(newPreviewModelList)
                        val selectedCount = count { model ->
                            model is UploadPreviewUiModel.Item
                        }
                        if (selectedCount < MAX_IMAGES_LIMIT) {
                            remainingPhotoCount = (MAX_IMAGES_LIMIT - selectedCount)
                            add(0, UploadPreviewUiModel.Placeholder(size))
                        }
                    }
                viewModelState.update { state ->
                    state.copy(
                        productFormData = formData.copy(
                            images = newModelList
                        )
                    )
                }
            }
            MediaType.Video -> {
                var remainingVideoCount = 0
                val newModelList = formData.videos
                    .filterNot { it is UploadPreviewUiModel.Placeholder }
                    .toMutableList().apply {
                        if (!append) {
                            clear()
                        }
                        addAll(newPreviewModelList)
                        val selectedCount = count { model ->
                            model is UploadPreviewUiModel.Item
                        }
                        if (selectedCount < MAX_VIDEOS_LIMIT) {
                            remainingVideoCount = (MAX_VIDEOS_LIMIT - selectedCount)
                            add(0, UploadPreviewUiModel.Placeholder(size))
                        }
                    }
                viewModelState.update { state ->
                    state.copy(
                        productFormData = formData.copy(
                            videos = newModelList
                        )
                    )
                }
            }
            else -> Noop()
        }
    }

    fun startUpload(context: Context): Job {
        setLoadState(loadType = LoadType.ACTION, loadState = LoadState.Loading())
        return viewModelScope.launch {
            StorageUtil.cleanUp(context)

            val productFormData = viewModelState.value.productFormData
            val uploadCallables = (productFormData.videos + productFormData.images)
                .filterIsInstance<UploadPreviewUiModel.Item>()
                .filter {
                    /* remoteFileName null means it is from local file, and needs to be uploaded */
                    it.sellerMediaFile.remoteFileName == null
                }
                .mapNotNull { model ->
                    val mimeType = getMimeType(context, model.sellerMediaFile.uri)
                    val tempFile = when (model.sellerMediaFile.mediaType) {
                        MediaType.Image -> {
                            StorageUtil.getTempUploadFile(context)
                        }
                        MediaType.Video -> {
                            StorageUtil.getTempUploadFile(
                                context,
                                "VID_",
                                EXTENSION_MP4
                            )
                        }
                        else -> null
                    }
                    tempFile ?: return@mapNotNull null
                    StorageUtil.saveFilesToFolder(
                        context,
                        model.sellerMediaFile.uri,
                        tempFile
                    )
                    model.sellerMediaFile.copy(cachedFile = tempFile)
                }
                .map { sellerMediaFile ->
                    FileUploaderCallable(
                        request = sellerMediaFile,
                        type = "product",
                        file = sellerMediaFile.cachedFile!!,
                        onProgress = { _ -> }
                    )
                }

            val jobPool = uploadCallables.map { callable ->
                viewModelScope.async(workerContext) {
                    withContext(Dispatchers.IO) {
                        callable.call()
                    }
                }
            }
            jobPool.awaitAll().let { callback ->
                callback.count { it.response?.data == null }.let { failedUploadCount ->
                    if (failedUploadCount > 0) {
                        Timber.w("$failedUploadCount upload(s) failed")
                    }
                }
                callback
            }.mapNotNull { callback ->
                Timber.d("Callback: $callback")
                // TODO: process success uploads
                val response = callback.response
                if (response?.data != null) {
                    callback.request.copy(
                        remoteFileName = response.data.fileName
                    )
                } else {
                    null
                }
            }.let { mediaFiles ->
                Timber.d("Upload response=$mediaFiles")
                val images = mediaFiles.filter { it.mediaType == MediaType.Image }
                val videos = mediaFiles.filter { it.mediaType == MediaType.Video }
                setPickedMediaUris(
                    MediaType.Image,
                    images.map(UploadPreviewUiModel::Item),
                    false
                )
                setPickedMediaUris(
                    MediaType.Video,
                    videos.map(UploadPreviewUiModel::Item),
                    false
                )
            }
            /*jobPool.count { it.data == null }.let { failedUploadCount ->
                if (failedUploadCount > 0) {
                    Timber.w("$failedUploadCount upload(s) failed")
                }
            }*/
            // submitReviewInternal()
            setLoadState(loadType = LoadType.ACTION, loadState = LoadState.NotLoading.Complete)
            // TODO: Handle upload complete.
            viewModelState.update { state ->
                state.copy(
                    uploadComplete = true,
                )
            }
        }
    }
    /* END - Upload related */

    private val threadCount: Int = Runtime.getRuntime().availableProcessors() * 2
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, t ->
            Timber.e(t)
        }
    private val backgroundDispatcher = newFixedThreadPoolContext(threadCount, "Upload photos pool")
    private val workerContext =
        backgroundDispatcher.limitedParallelism(MAX_PARALLEL_THREADS) + SupervisorJob() + coroutineExceptionHandler

    companion object {
        private const val MAX_PARALLEL_THREADS: Int = 2
    }
}

interface AddProductUiAction {
    data class ErrorShown(val id: Long) : AddProductUiAction
    data class ToggleSelectedCategory(val productId: Long, val category: ProductCategory) : AddProductUiAction
    data class Submit(val context: Context) : AddProductUiAction
    data object Reset : AddProductUiAction
    data object Refresh: AddProductUiAction
}

interface AddProductUiEvent {
    data class ShowToast(val message: UiText) : AddProductUiEvent
    data class ShowSnack(val message: UiText) : AddProductUiEvent
}

interface ProductCategoriesUiState {
    data class CategoriesList(
        val productCategories: List<ProductCategory>,
        val selectedCategory: ProductCategory?,
        val isLoading: Boolean,
    ) : ProductCategoriesUiState
    data class Error(val exception: Exception) : ProductCategoriesUiState
    data object Loading : ProductCategoriesUiState

    data object Idle : ProductCategoriesUiState
}

data class ProductUiState(
    val productFormData: ProductFormData = ProductFormData(id = 0,),
    val mediaUploaded: Boolean = false,
    val productAdded: Boolean = false
)

private data class AddProductViewModelState(
    val productCategoryLoadState: LoadStates = LoadStates.IDLE,
    val productCategories: List<ProductCategory> = emptyList(),
    val selectedProductCategory: ProductCategory? = null,

    val loadState: LoadStates = LoadStates.IDLE,

    val productFormData: ProductFormData = ProductFormData(id = 0,),
    val uploadComplete: Boolean = false,
    /**
     * Flag to indicate that the product is added. Used for navigation.
     */
    val isProductAddedSuccessfully: Boolean = false,
) {
    fun toProductCategoriesUiState(): ProductCategoriesUiState {
        return if (productCategories.isEmpty()) {
            when (val loadState = productCategoryLoadState.refresh) {
                is LoadState.Loading -> {
                    ProductCategoriesUiState.Loading
                }
                is LoadState.Error -> {
                    ProductCategoriesUiState.Error(
                        Exception(loadState.error)
                    )
                }

                else -> ProductCategoriesUiState.Idle
            }
        } else {
            ProductCategoriesUiState.CategoriesList(
                productCategories = productCategories,
                selectedCategory = selectedProductCategory,
                isLoading = productCategoryLoadState.refresh is LoadState.Loading
            )
        }
    }


    fun toProductUiState(): ProductUiState {
        return ProductUiState(
            productFormData = productFormData,
            mediaUploaded = uploadComplete,
            productAdded = isProductAddedSuccessfully
        )
    }
}

private const val KEY_CACHED_UPLOAD_SESSION_ID = "cached_session_upload_id"