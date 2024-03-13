package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BleedingEntryViewModel(private val bleedingRepository: BleedingRepository) : ViewModel() {


    var uiState by mutableStateOf(BleedingEventUiState())
        private set


    fun updateUiState(bleedingDetails: BleedingDetails) {
        uiState = BleedingEventUiState(bleedingDetails, validateInput(bleedingDetails))
    }

    fun reset() {
        uiState = BleedingEventUiState(BleedingDetails())
    }

    // fix severity button error
    private fun validateInput(bleedingDetails: BleedingDetails = uiState.bleedingDetails): Boolean {
        return with(bleedingDetails) {
            site.isNotBlank()
                    && cause.isNotBlank()
                    && painScale.isNotBlank()
                    && severity.isNotBlank()
                    && date.isNotBlank()
                    && time.isNotBlank()

        }
    }

    suspend fun onSave() {
        if (validateInput())
            bleedingRepository.insertItem(uiState.bleedingDetails.toEntity())
    }
}

data class BleedingEventUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val isEntryValid: Boolean = false
)

data class BleedingDetails(
    val id: Int = 0,
    val site: String = "",
    val cause: String = "",
    val severity: String = "",
    val painScale: String = "",
    val note: String? = null,
    val date: String = SimpleDateFormat("dd-MM-yyyy").format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
)

fun BleedingDetails.toEntity(): BleedingEvent =
    BleedingEvent(
        id = id,
        bleedingSite = site,
        bleedingCause = cause,
        severity = severity,
        painScale = painScale,
        note = note,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.plus(" $time")).time
    )

fun BleedingEvent.toBleedingDetails(): BleedingDetails =
    BleedingDetails(
        id = id,
        site = bleedingSite,
        cause = bleedingCause,
        severity = severity,
        painScale = painScale,
        note = note,
        date = SimpleDateFormat("dd-MM-yyyy").format(Date(timestamp)),
        time = SimpleDateFormat("HH:mm").format(Date(timestamp)),
    )

fun BleedingEvent.toBleedingUiState(): BleedingEventUiState =
    BleedingEventUiState(
        bleedingDetails = this.toBleedingDetails(),
    )