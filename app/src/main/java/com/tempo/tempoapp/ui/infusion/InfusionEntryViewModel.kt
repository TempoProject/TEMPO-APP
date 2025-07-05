package com.tempo.tempoapp.ui.infusion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * ViewModel for managing the state of the infusion entry screen.
 *
 * @param infusionRepository Repository for accessing infusion data.
 */
class InfusionEntryViewModel(private val infusionRepository: InfusionRepository) : ViewModel() {
    var uiState by mutableStateOf(InfusionUiState())
        private set

    /**
     * Updates the UI state with the provided infusion details and validation result.
     *
     * @param infusionDetails Details of the infusion event.
     */
    fun updateUiState(infusionDetails: InfusionDetails) {
        uiState = uiState.copy(infusionDetails = infusionDetails)
    }

    /**
     * Validates the input for the infusion event.
     *
     * @param infusionDetails Details of the infusion event.
     * @return True if the input is valid, false otherwise.
     */
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
            }


            if (time.isBlank()) {
                errors["time"] = R.string.error_time_required
            }

        }

        return errors
    }

    /**
     * Saves the infusion event if the input is valid.
     */
    suspend fun onSave(): Boolean {
        val validationErrors = validateInput()

        uiState = uiState.copy(
            isEntryValid = validationErrors.isEmpty(),
            validationErrors = validationErrors,
            hasAttemptedSave = true
        )

        return if (validationErrors.isEmpty()) {
            infusionRepository.insertItem(uiState.infusionDetails.toEntity())
            true
        } else {
            false
        }
    }

}

/**
 * UI state for the infusion entry screen.
 *
 * @property infusionDetails Details of the infusion event.
 * @property isEntryValid Indicates whether the entry is valid or not.
 */
data class InfusionUiState(
    val infusionDetails: InfusionDetails = InfusionDetails(),
    val isEntryValid: Boolean = false,
    val validationErrors: Map<String, Int> = emptyMap(),
    val hasAttemptedSave: Boolean = false,
    val isLoading: Boolean = false
) {
    /**
     * Checks if a specific field has an error and user has attempted save
     */
    fun hasError(fieldName: String): Boolean =
        hasAttemptedSave && validationErrors.containsKey(fieldName)

    /**
     * Gets the error message for a specific field
     */
    fun getError(fieldName: String): Int? =
        if (hasAttemptedSave) validationErrors[fieldName] else null

    /**
     * Should show errors only if user has attempted to save
     */
    fun shouldShowErrors(): Boolean = hasAttemptedSave && validationErrors.isNotEmpty()
}


/**
 * Converts infusion details to an entity for database operations.
 *
 * @receiver Details of the infusion event.
 * @return Infusion event entity.
 */
fun InfusionDetails.toEntity(): InfusionEvent =
    InfusionEvent(
        id = id,
        reason = reason,
        drugName = drugName,
        dose = dose,
        dosageUnit = dosageUnit.name,
        batchNumber = batchNumber.ifBlank { null },
        note = note,
        date = date,
        isSent = false,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time"))?.time ?: Instant.now().toEpochMilli()
    )


/**
 * Converts an infusion event to infusion details.
 *
 * @receiver Infusion event.
 * @return Infusion details.
 */
fun InfusionEvent.toInfusionDetails(): InfusionDetails =
    InfusionDetails(
        id = id,
        reason = reason ?: "",
        drugName = drugName ?: "",
        dose = dose ?: "",
        dosageUnit = DosageUnit.valueOf(
            dosageUnit ?: DosageUnit.MG_KG.name
        ),
        batchNumber = batchNumber ?: "",
        note = note,
        date = date,
        time = timestamp.toStringTime()
    )

/**
 * Converts an infusion event to infusion UI state.
 *
 * @receiver Infusion event.
 * @return Infusion UI state.
 */
fun InfusionEvent.toInfusionUiState(): InfusionUiState =
    InfusionUiState(
        this.toInfusionDetails()
    )

data class InfusionDetails(
    val id: Int = 0,
    val reason: String = "",
    val drugName: String = "",
    val dose: String = "",
    val dosageUnit: DosageUnit = DosageUnit.MG_KG,
    val batchNumber: String = "",
    val note: String? = null,
    val date: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
    val time: String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime(),
)