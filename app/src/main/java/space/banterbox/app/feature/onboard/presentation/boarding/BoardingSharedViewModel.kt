package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoardingSharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel()