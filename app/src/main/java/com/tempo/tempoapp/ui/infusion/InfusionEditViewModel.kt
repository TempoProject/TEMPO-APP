package com.tempo.tempoapp.ui.infusion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.InfusionRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the state of the screen for editing infusion details.
 *
 * @param savedStateHandle SavedStateHandle to retrieve the ID of the infusion event being edited.
 * @param infusionRepository Repository for accessing infusion-related data.
 */
class InfusionEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val infusionRepository: InfusionRepository
) : ViewModel() {

    /** ID of the infusion event being edited. */
    private val eventId: Int =
        checkNotNull(savedStateHandle[InfusionEditDestination.itemIdArg])

    /** Mutable state for holding the UI state of the infusion edit screen. */
    var uiState by mutableStateOf(InfusionUiState())
        private set

    init {
        viewModelScope.launch {
            // Retrieve the infusion details from the repository and initialize the UI state
            uiState = infusionRepository.getItemFromId(eventId).filterNotNull().first()
                .toInfusionUiState()
        }
    }

    /**
     * Updates the infusion details in the repository if the input is valid.
     */
    suspend fun update() {
        if (validateInput())
            infusionRepository.updateItem(uiState.infusionDetails.toEntity())
    }

    /**
     * Updates the UI state based on the provided infusion details.
     *
     * @param infusionDetails Updated infusion details.
     */
    fun updateUiState(infusionDetails: InfusionDetails) {
        uiState = InfusionUiState(infusionDetails, validateInput(infusionDetails))
    }

    /**
     * Validates the input for infusion details.
     *
     * @param infusionDetails Infusion details to validate.
     * @return True if the input is valid, false otherwise.
     */
    private fun validateInput(infusionDetails: InfusionDetails = uiState.infusionDetails): Boolean {
        return with(infusionDetails) {
            treatment.isNotBlank() &&
                    infusionSite.isNotBlank() &&
                    doseUnits.isNotBlank() &&
                    lotNumber.isNotBlank() &&
                    //date.isNotBlank() &&
                    time.isNotBlank()
        }
    }
}