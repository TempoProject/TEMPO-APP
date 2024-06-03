package com.tempo.tempoapp.ui.infusion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.repository.InfusionRepository
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
        uiState = InfusionUiState(infusionDetails, validateInput(infusionDetails))
    }

    /**
     * Validates the input for the infusion event.
     *
     * @param infusionDetails Details of the infusion event.
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

    /**
     * Saves the infusion event if the input is valid.
     */
    suspend fun onSave() {
        if (validateInput())
            infusionRepository.insertItem(uiState.infusionDetails.toEntity())
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
    val isEntryValid: Boolean = false
)

/**
 * Model class representing details of an infusion event.
 *
 * @property id Unique identifier of the infusion event.
 * @property treatment Type of treatment for the infusion.
 * @property infusionSite Site of the infusion.
 * @property doseUnits Units of dose for the infusion.
 * @property lotNumber Lot number of the infusion.
 * @property note Additional note for the infusion.
 * @property date Date of the infusion event.
 * @property time Time of the infusion event.
 */
data class InfusionDetails(
    val id: Int = 0,
    val treatment: String = "",
    val infusionSite: String = "",
    val doseUnits: String = "",
    val lotNumber: String = "",
    val note: String? = null,
    val date: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
    val time: String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime(),
)

/**
 * Converts infusion details to an entity for database operations.
 *
 * @receiver Details of the infusion event.
 * @return Infusion event entity.
 */
fun InfusionDetails.toEntity(): InfusionEvent =
    InfusionEvent(
        id = id,
        treatment = treatment,
        infusionSite = infusionSite,
        doseUnits = doseUnits.toInt(),
        lotNumber = lotNumber.toInt(),
        note = note,
        date = date,
        isSent = false,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time")).time
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
        infusionSite = infusionSite,
        treatment = treatment,
        lotNumber = lotNumber.toString(),
        doseUnits = doseUnits.toString(),
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