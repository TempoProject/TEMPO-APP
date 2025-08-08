package com.tempo.tempoapp.ui.infusion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.utils.CrashlyticsHelper
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
        infusionRepository.getItemFromId(itemId)
            .filterNotNull()
            .map { infusionEvent ->
                InfusionDetailsUiState(
                    infusionDetails = infusionEvent.toInfusionDetails(),
                    id = infusionEvent.id,
                    isLoading = false
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = InfusionDetailsUiState(isLoading = true)
            )

    /**
     * Deletes the infusion item.
     */
    suspend fun deleteItem() {
        try {
            val currentState = uiState.value
            if (!currentState.isLoading && currentState.id != -1) {
                infusionRepository.deleteItem(currentState.infusionDetails.toEntity())

                CrashlyticsHelper.logCriticalAction(
                    action = "infusion_event_delete",
                    success = true,
                    details = "Infusion event deleted successfully"
                )
            } else {

                CrashlyticsHelper.logCriticalAction(
                    action = "infusion_event_delete",
                    success = false,
                    details = "Invalid delete attempt - loading: ${currentState.isLoading}, id: ${currentState.id}"
                )
            }

        } catch (e: Exception) {

            CrashlyticsHelper.logCriticalAction(
                action = "infusion_event_delete",
                success = false,
                details = "Exception occurred: ${e.message}"
            )
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class InfusionDetailsUiState(
    val infusionDetails: InfusionDetails = InfusionDetails(),
    val id: Int = -1,
    val isLoading: Boolean = false
)