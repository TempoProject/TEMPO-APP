package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.BleedingRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * View model for the bleeding event edit screen.
 * @param savedStateHandle The saved state handle for accessing saved state information.
 * @param bleedingRepository The repository for managing bleeding event data.
 */
class BleedingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val bleedingRepository: BleedingRepository
) : ViewModel() {

    /** The ID of the bleeding event being edited. */
    private val eventId: Int =
        checkNotNull(savedStateHandle[BleedingEventEditDestination.itemIdArg])

    /** Mutable state variable for the UI state of the edit screen. */
    var uiState by mutableStateOf(BleedingEventUiState(isLoading = true))
        private set

    init {
        // Initialize UI state by loading the bleeding event data
        viewModelScope.launch {
            uiState = bleedingRepository.getItemFromId(eventId).filterNotNull().first()
                .toBleedingUiState()
        }.invokeOnCompletion {
            uiState = uiState.copy(isLoading = false)
        }
    }

    /**
     * Updates the bleeding event data.
     * This method is called when the user saves the changes.
     */
    suspend fun update() {
        if (validateInput(uiState.bleedingDetails))
            bleedingRepository.updateItem(uiState.bleedingDetails.toEntity())
    }

    /**
     * Updates the UI state with the provided bleeding details.
     * This method is called when the user interacts with the input fields.
     * @param details The updated bleeding details.
     */
    fun updateUiState(details: BleedingDetails) {
        uiState = BleedingEventUiState(
            details, validateInput(details)
        )
    }

    /**
     * Validates the input fields of the bleeding details.
     * @param bleedingDetails The bleeding details to be validated.
     * @return True if all required fields are filled, false otherwise.
     */
    private fun validateInput(bleedingDetails: BleedingDetails = uiState.bleedingDetails): Boolean {
        return with(bleedingDetails) {
            site.isNotBlank()
                    && cause.isNotBlank()
                    && painScale.isNotBlank()
                    && time.isNotBlank()

        }
    }
}

