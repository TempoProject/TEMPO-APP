package com.tempo.tempoapp.ui.infusion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.R
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
            try {
                val infusionEvent =
                    infusionRepository.getItemFromId(eventId).filterNotNull().first()
                uiState = infusionEvent.toInfusionUiState().copy(
                    isLoading = false,
                    isEntryValid = true
                )
            } catch (e: Exception) {
                uiState = InfusionUiState(
                    isLoading = false,
                    isEntryValid = false
                )
            }
        }
    }

    fun updateUiState(infusionDetails: InfusionDetails) {
        uiState = uiState.copy(
            infusionDetails = infusionDetails,
            isEntryValid = true
        )
    }

    suspend fun update(): Boolean {
        val validationErrors = validateInput()

        uiState = uiState.copy(
            isEntryValid = validationErrors.isEmpty(),
            validationErrors = validationErrors,
            hasAttemptedSave = true
        )

        return if (validationErrors.isEmpty()) {
            try {
                infusionRepository.updateItem(uiState.infusionDetails.toEntity())
                true
            } catch (e: Exception) {
                // Handle update error
                false
            }
        } else {
            false
        }
    }

    private fun validateInput(infusionDetails: InfusionDetails = uiState.infusionDetails): Map<String, Int> {
        val errors = mutableMapOf<String, Int>()

        with(infusionDetails) {
            if (reason.isBlank()) {
                errors["reason"] = R.string.error_reason_required
            }


            if (drugName.isBlank()) {
                errors["drugName"] =
                    R.string.error_medication_type_required
            }


            if (dose.isBlank()) {
                errors["dose"] = R.string.error_dose_required
            } else {
                val normalizedDose = dose.replace(",", ".")
                try {
                    val doseValue = normalizedDose.toDouble()
                    if (doseValue <= 0) {
                        errors["dose"] = R.string.error_dose_must_be_positive
                    }
                } catch (e: NumberFormatException) {
                    errors["dose"] = R.string.error_dose_invalid_format
                }
            }


            if (time.isBlank()) {
                errors["time"] = R.string.error_time_required
            }

        }

        return errors
    }
}