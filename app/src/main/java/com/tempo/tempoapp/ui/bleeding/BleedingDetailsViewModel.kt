package com.tempo.tempoapp.ui.bleeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.utils.CrashlyticsHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel class for managing the UI state of Bleeding Event Details screen.
 * @param savedStateHandle The saved state handle for handling state persistence across configuration changes.
 * @param bleedingRepository The repository for accessing bleeding event data.
 */
class BleedingDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val bleedingRepository: BleedingRepository
) : ViewModel() {
    // Extracting the item ID from the saved state handle
    private val itemId: Int =
        checkNotNull(savedStateHandle[BleedingEventDetailsDestination.itemIdArg])

    // State flow representing the UI state of the bleeding details screen
    val uiState: StateFlow<BleedingDetailsUiState> =
        bleedingRepository.getItemFromId(itemId)
            .filterNotNull()
            .map { bleedingEvent ->
                BleedingDetailsUiState(
                    bleedingDetails = bleedingEvent.toBleedingDetails(),
                    id = bleedingEvent.id,
                    isLoading = false
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = BleedingDetailsUiState(isLoading = true)
            )

    /**
     * Deletes the currently displayed bleeding event item.
     */
    suspend fun deleteItem() {
        try {
            val currentState = uiState.value
            if (!currentState.isLoading && currentState.id != -1) {
                bleedingRepository.deleteItem(currentState.bleedingDetails.toEntity())


                CrashlyticsHelper.logCriticalAction(
                    action = "bleeding_event_delete",
                    success = true,
                    details = "Bleeding event deleted successfully"
                )
            } else {

                CrashlyticsHelper.logCriticalAction(
                    action = "bleeding_event_delete",
                    success = false,
                    details = "Invalid delete attempt - loading: ${currentState.isLoading}, id: ${currentState.id}"
                )
            }

        } catch (e: Exception) {
            CrashlyticsHelper.logCriticalAction(
                action = "bleeding_event_delete",
                success = false,
                details = "Exception occurred: ${e.message}"
            )
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}


data class BleedingDetailsUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val id: Int = -1,
    val isLoading: Boolean = false
)