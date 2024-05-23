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

/**
 * View model for managing infusion details.
 *
 * @param savedStateHandle SavedStateHandle to handle the state of the view model.
 * @param infusionRepository Repository for accessing infusion data.
 */
class InfusionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val infusionRepository: InfusionRepository
) : ViewModel() {
    // Extract item ID from SavedStateHandle
    private val itemId: Int = checkNotNull(savedStateHandle[InfusionDetailsDestination.itemIdArg])

    // UI state representing infusion details
    val uiState: StateFlow<InfusionDetailsUiState> =
        infusionRepository.getItemFromId(itemId).filterNotNull().map {
            InfusionDetailsUiState(it.toInfusionDetails(), itemId)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = InfusionDetailsUiState()
        )

    /**
     * Deletes the infusion item.
     */
    suspend fun deleteItem() {
        infusionRepository.deleteItem(uiState.value.infusionDetails.toEntity())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Represents the UI state for infusion details.
 *
 * @param infusionDetails Details of the infusion.
 * @param id ID of the infusion item.
 */
data class InfusionDetailsUiState(
    val infusionDetails: InfusionDetails = InfusionDetails(),
    val id: Int = -1
)