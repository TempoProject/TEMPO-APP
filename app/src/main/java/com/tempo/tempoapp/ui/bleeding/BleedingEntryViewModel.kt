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
                    //&& severity.isNotBlank()
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
 * Represents the UI state of the bleeding event.
 *
 * @property bleedingDetails Details of the bleeding event.
 * @property isEntryValid Flag indicating whether the bleeding event entry is valid.
 * @property isLoading Flag indicating whether the data is loaded.
 */
data class BleedingEventUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val isEntryValid: Boolean = false,
    val isLoading: Boolean = false
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
    //val severity: String = "",
    val painScale: String = "".ifEmpty { "0.0" },
    val isABleedingEpisode: String = "".ifEmpty { "No" },
    val questionBleedingEpisode: String = "".ifEmpty { "No" },
    val treatment: String = "".ifEmpty { "No" },
    val note: String? = null,
    val date: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
    val time: String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime(),
)

/**
 * Extension function to convert BleedingDetails to BleedingEvent entity.
 *
 * @receiver BleedingDetails object.
 * @return Corresponding BleedingEvent entity.
 */
fun BleedingDetails.toEntity(): BleedingEvent =
    BleedingEvent(
        id = id,
        bleedingSite = site,
        bleedingCause = cause,
        //severity = severity,
        painScale = painScale,
        note = note,
        date = date,
        isSent = false,
        questionBleedingEpisode = questionBleedingEpisode,
        isABleedingEpisode = isABleedingEpisode,
        treatment = treatment,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time")).time
    )

/**
 * Extension function to convert BleedingEvent to BleedingDetails.
 *
 * @receiver BleedingEvent object.
 * @return Corresponding BleedingDetails object.
 */
fun BleedingEvent.toBleedingDetails(): BleedingDetails =
    BleedingDetails(
        id = id,
        site = bleedingSite,
        cause = bleedingCause,
        //severity = severity,
        painScale = painScale,
        questionBleedingEpisode = questionBleedingEpisode,
        isABleedingEpisode = isABleedingEpisode,
        treatment = treatment,
        note = note,
        date = date,
        time = timestamp.toStringTime()
    )

/**
 * Extension function to convert BleedingEvent to BleedingEventUiState.
 *
 * @receiver BleedingEvent object.
 * @return Corresponding BleedingEventUiState object.
 */
fun BleedingEvent.toBleedingUiState(): BleedingEventUiState =
    BleedingEventUiState(
        bleedingDetails = this.toBleedingDetails(),
    )