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

class InfusionEntryViewModel(private val infusionRepository: InfusionRepository) : ViewModel() {
    var uiState by mutableStateOf(InfusionUiState())
        private set

    fun updateUiState(infusionDetails: InfusionDetails) {
        uiState = InfusionUiState(infusionDetails, validateInput(infusionDetails))
    }

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

    suspend fun onSave() {
        if (validateInput())
            infusionRepository.insertItem(uiState.infusionDetails.toEntity())
    }

}

data class InfusionUiState(
    val infusionDetails: InfusionDetails = InfusionDetails(),
    val isEntryValid: Boolean = false
)

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

fun InfusionDetails.toEntity(): InfusionEvent =
    InfusionEvent(
        id = id,
        treatment = treatment,
        infusionSite = infusionSite,
        doseUnits = doseUnits.toInt(),
        lotNumber = lotNumber.toInt(),
        note = note,
        date = date,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.toStringDate().plus(" $time")).time
    )

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

fun InfusionEvent.toInfusionUiState(): InfusionUiState =
    InfusionUiState(
        this.toInfusionDetails()
    )