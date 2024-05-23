package com.tempo.tempoapp.ui.bleeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.BleedingRepository
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
        bleedingRepository.getItemFromId(itemId).filterNotNull().map {
            BleedingDetailsUiState(bleedingDetails = it.toBleedingDetails(), itemId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = BleedingDetailsUiState()
        )

    /**
     * Deletes the currently displayed bleeding event item.
     */
    suspend fun deleteItem() {
        bleedingRepository.deleteItem(uiState.value.bleedingDetails.toEntity())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

/**
 * Represents the UI state for displaying bleeding event details.
 * @param bleedingDetails The details of the bleeding event.
 * @param id The ID of the bleeding event.
 */
data class BleedingDetailsUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val id: Int = -1
)