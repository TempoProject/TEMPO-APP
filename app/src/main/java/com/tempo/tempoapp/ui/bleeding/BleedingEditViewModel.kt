package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.utils.CrashlyticsHelper
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


    private val eventId: Int =
        checkNotNull(savedStateHandle[BleedingEventEditDestination.itemIdArg])

    var uiState by mutableStateOf(BleedingEventUiState(isLoading = true))
        private set

    init {
        viewModelScope.launch {
            try {
                val bleedingEvent =
                    bleedingRepository.getItemFromId(eventId).filterNotNull().first()
                uiState = bleedingEvent.toBleedingUiState().copy(
                    isLoading = false,
                    isEntryValid = true
                )
            } catch (e: Exception) {
                uiState = BleedingEventUiState(
                    isLoading = false,
                    isEntryValid = false
                )
            }
        }
    }

    private fun validateInput(
        bleedingDetails: BleedingDetails = uiState.bleedingDetails,
    ): Map<String, Int> {
        val errors = mutableMapOf<String, Int>()

        with(bleedingDetails) {

            if (eventType.isBlank()) {
                errors["eventType"] = R.string.error_event_type_required
            }


            if (cause.isBlank()) {
                errors["cause"] = R.string.error_cause_required
            }


            if (site.isBlank()) {
                errors["site"] = R.string.error_site_required
            }


            if (site == "Other" && note.isNullOrBlank()) {
                errors["note"] = R.string.error_notes_required_for_other
            }


            if (treatment.isBlank()) {
                errors["treatment"] = R.string.error_treatment_required
            }


            if (treatment == "Sì") {
                if (medicationType.isBlank()) {
                    errors["medicationType"] = R.string.error_medication_type_required
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

            }


            if (painScale.isBlank()) {
                errors["painScale"] = R.string.error_pain_scale_required
            }


            if (time.isBlank()) {
                errors["time"] = R.string.error_time_required
            }
        }

        return errors
    }

    /**
     * Updates the UI state with the provided bleeding details.
     * This method is called when the user interacts with the input fields.
     * @param details The updated bleeding details.
     */
    fun updateUiState(details: BleedingDetails) {
        uiState = uiState.copy(
            bleedingDetails = details,
            isEntryValid = true
        )
    }


    /**
     * Updates the bleeding event data.
     * This method is called when the user saves the changes.
     * Triggers validation only when called.
     */
    suspend fun update(): Boolean {
        return try {
            val validationErrors = validateInput()

            uiState = uiState.copy(
                isEntryValid = validationErrors.isEmpty(),
                validationErrors = validationErrors,
                hasAttemptedSave = true
            )

            if (validationErrors.isEmpty()) {
                bleedingRepository.updateItem(uiState.bleedingDetails.toEntity())

                CrashlyticsHelper.logCriticalAction(
                    action = "bleeding_event_update",
                    success = true,
                    details = "Bleeding event updated successfully"
                )

                true
            } else {
                CrashlyticsHelper.logCriticalAction(
                    action = "bleeding_event_update",
                    success = false,
                    details = "Validation failed: ${validationErrors.size} errors"
                )

                false
            }

        } catch (e: Exception) {
            CrashlyticsHelper.logCriticalAction(
                action = "bleeding_event_update",
                success = false,
                details = "Exception occurred: ${e.message}"
            )

            false
        }
    }
}

