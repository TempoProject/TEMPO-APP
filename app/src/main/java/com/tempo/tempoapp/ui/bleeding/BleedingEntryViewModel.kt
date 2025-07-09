package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * ViewModel responsible for managing the state and logic of the Bleeding Entry screen.
 *
 * @param bleedingRepository Repository for interacting with bleeding event data.
 */
class BleedingEntryViewModel(private val bleedingRepository: BleedingRepository) : ViewModel() {

    /**
     * UI state of the bleeding event.
     */
    var uiState by mutableStateOf(BleedingEventUiState())
        private set


    /**
     * Updates the UI state based on the provided bleeding details.
     *
     * @param bleedingDetails Details of the bleeding event.
     */
    fun updateUiState(bleedingDetails: BleedingDetails) {
        uiState = uiState.copy(
            bleedingDetails = bleedingDetails
        )
    }

    /**
     * Validates the input fields of the bleeding event.
     *
     * @param bleedingDetails Details of the bleeding event to validate.
     * @return True if all required fields are not blank, false otherwise.
     */
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


            if ((site == "Other" || site == "Altro") && note.isNullOrBlank()) {
                errors["note"] = R.string.error_notes_required_for_other
            }


            if (treatment.isBlank()) {
                errors["treatment"] = R.string.error_treatment_required
            }


            if (treatment == "Si" || treatment == "Yes") {
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
     * Saves the bleeding event if the input is valid.
     */
    suspend fun onSave(): Boolean {
        val validationErrors = validateInput()

        uiState = uiState.copy(
            isEntryValid = validationErrors.isEmpty(),
            validationErrors = validationErrors,
            hasAttemptedSave = true
        )

        return if (validationErrors.isEmpty()) {
            bleedingRepository.insertItem(uiState.bleedingDetails.toEntity())
            true
        } else {
            false
        }
    }
}

/**
 * Represents the UI state of the bleeding event.
 *
 * @property bleedingDetails Details of the bleeding event.
 * @property isEntryValid Flag indicating whether the bleeding event entry is valid.
 * @property isLoading Flag indicating whether the data is loaded.
 */
data class BleedingEventUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
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

data class BleedingDetails(
    val id: Int = 0,
    val eventType: String = "",
    val cause: String = "",
    val site: String = "",
    val treatment: String = "",
    val medicationType: String = "",
    val dose: String = "",
    val dosageUnit: DosageUnit = DosageUnit.IU,
    val lotNumber: String = "",
    val painScale: String = "0",
    val note: String? = null, // Note (facoltativo, ma obbligatorio se site = "Other")
    val date: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
    val time: String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime(),
)

fun BleedingEvent.toBleedingDetails(): BleedingDetails =
    BleedingDetails(
        id = id,
        site = bleedingSite,
        cause = bleedingCause,
        eventType = eventType ?: "",
        painScale = painScale,
        medicationType = medicationType ?: "",
        dose = dose ?: "",
        dosageUnit = DosageUnit.valueOf(
            dosageUnit ?: DosageUnit.MG_KG.name
        ),
        lotNumber = lotNumber ?: "",
        treatment = treatment,
        note = note,
        date = date,
        time = timestamp.toStringTime()
    )

fun BleedingDetails.toEntity(): BleedingEvent =
    BleedingEvent(
        id = id,
        eventType = eventType,
        bleedingSite = site,
        bleedingCause = cause,
        treatment = treatment,
        medicationType = medicationType.ifBlank { null },
        dose = dose.ifBlank { null },
        dosageUnit = dosageUnit.name,
        lotNumber = lotNumber.ifBlank { null },
        painScale = painScale,
        note = note,
        date = date,
        isSent = false,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time"))?.time ?: Instant.now().toEpochMilli()
    )

fun BleedingEvent.toBleedingUiState(): BleedingEventUiState =
    BleedingEventUiState(
        bleedingDetails = this.toBleedingDetails(),
    )