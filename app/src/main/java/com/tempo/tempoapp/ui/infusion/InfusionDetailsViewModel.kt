package com.tempo.tempoapp.ui.infusion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.InfusionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InfusionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val infusionRepository: InfusionRepository
) : ViewModel() {
    private val itemId: Int = checkNotNull(savedStateHandle[InfusionDetailsDestination.itemIdArg])

    val uiState: StateFlow<InfusionDetailsUiState> =
        infusionRepository.getItemFromId(itemId).filterNotNull().map {
            InfusionDetailsUiState(it.toInfusionDetails(), itemId)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = InfusionDetailsUiState()
        )


    suspend fun deleteItem() {
        infusionRepository.deleteItem(uiState.value.infusionDetails.toEntity())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class InfusionDetailsUiState(
    val infusionDetails: InfusionDetails = InfusionDetails(),
    val id: Int = -1
)