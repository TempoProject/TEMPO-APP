package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
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
        uiState = BleedingEventUiState(bleedingDetails, validateInput(bleedingDetails))
    }

    /**
     * Resets the UI state to default.
     */
    fun reset() {
        uiState = BleedingEventUiState(BleedingDetails())
    }


    /**
     * Validates the input fields of the bleeding event.
     *
     * @param bleedingDetails Details of the bleeding event to validate.
     * @return True if all required fields are not blank, false otherwise.
     */
    private fun validateInput(bleedingDetails: BleedingDetails = uiState.bleedingDetails): Boolean {
        return with(bleedingDetails) {
            site.isNotBlank()
                    && cause.isNotBlank()
                    && painScale.isNotBlank()
                    && severity.isNotBlank()
                    //&& date.isNotBlank()
                    && time.isNotBlank()

        }
    }

    /**
     * Saves the bleeding event if the input is valid.
     */
    suspend fun onSave() {
        if (validateInput())
            bleedingRepository.insertItem(uiState.bleedingDetails.toEntity())
    }
}

/**
 * UI state of the bleeding event screen.
 *
 * @property bleedingDetails Details of the bleeding event.
 * @property isEntryValid Flag indicating if the entry is valid.
 */
data class BleedingEventUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val isEntryValid: Boolean = false
)

/**
 * Represents the details of a bleeding event.
 *
 * @property id Identifier of the bleeding event.
 * @property site Site of the bleeding event.
 * @property cause Cause of the bleeding event.
 * @property severity Severity of the bleeding event.
 * @property painScale Pain scale of the bleeding event.
 * @property note Additional note for the bleeding event.
 * @property date Date of the bleeding event.
 * @property time Time of the bleeding event.
 */
data class BleedingDetails(
    val id: Int = 0,
    val site: String = "",
    val cause: String = "",
    val severity: String = "",
    val painScale: String = "",
    val note: String? = null,
    val date: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
    val time: String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime(),
)

/**
 * Extension function to convert BleedingEvent entity to BleedingDetails.
 *
 * @receiver BleedingEvent object.
 * @return Corresponding BleedingDetails.
 */
fun BleedingDetails.toEntity(): BleedingEvent =
    BleedingEvent(
        id = id,
        bleedingSite = site,
        bleedingCause = cause,
        severity = severity,
        painScale = painScale,
        note = note,
        date = date,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time")).time
    )

/**
 * Extension function to convert BleedingEvent entity to BleedingEventUiState.
 *
 * @receiver BleedingEvent object.
 * @return Corresponding BleedingEventUiState.
 */
fun BleedingEvent.toBleedingDetails(): BleedingDetails =
    BleedingDetails(
        id = id,
        site = bleedingSite,
        cause = bleedingCause,
        severity = severity,
        painScale = painScale,
        note = note,
        date = date,
        time = timestamp.toStringTime()
    )

fun BleedingEvent.toBleedingUiState(): BleedingEventUiState =
    BleedingEventUiState(
        bleedingDetails = this.toBleedingDetails(),
    )