package com.tempo.tempoapp.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.ProphylaxisResponseRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.ui.BodyEvent
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * ViewModel responsible for managing the state and logic of the history screen.
 *
 * @param bleedingRepository Repository for managing bleeding events.
 * @param infusionRepository Repository for managing infusion events.
 * @param stepsRecordRepository Repository for managing steps records.
 */
class HistoryViewModel(
    private val bleedingRepository: BleedingRepository,
    private val infusionRepository: InfusionRepository,
    private val stepsRecordRepository: StepsRecordRepository,
    prophylaxisResponseRepository: ProphylaxisResponseRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    )
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()


    // State flow to emit the UI state combining bleeding events, infusion events, and steps count
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyUiState: StateFlow<HistoryUiState> = _selectedDate
        .flatMapLatest { timestamp ->
            combine(
                bleedingRepository.getAllDayBleeding(timestamp),
                infusionRepository.getAllDayInfusion(timestamp),
                stepsRecordRepository.getAllDayStepsCount(timestamp),
                prophylaxisResponseRepository.getAllDayProphylaxis(timestamp),
            ) { bleeding, infusion, steps, prophylaxis ->

                Log.d(
                    "HistoryViewModel",
                    "Selected date: ${_selectedDate.value}"
                )
                Log.d(
                    "HistoryViewModel",
                    "Fetched data for date: ${timestamp.toStringDate()}, " +
                            "Bleeding: ${bleeding.size}, Infusion: ${infusion.size}, " +
                            "Steps: $steps, Prophylaxis: ${prophylaxis.size}"
                )
                HistoryUiState(bleeding, infusion, prophylaxis, steps)
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = HistoryUiState(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )


    fun updateSelectedDate(newTimestamp: Long) {
        val truncatedTimestamp = Instant.ofEpochMilli(newTimestamp)
            .truncatedTo(ChronoUnit.DAYS)
            .toEpochMilli()
        _selectedDate.value = truncatedTimestamp
    }

    /**
     * Aggiorna la data selezionata passando un LocalDate (versione di convenienza)
     */
    fun updateSelectedDate(newDate: LocalDate) {
        Log.d("HistoryViewModel", "=== UTC CONVERSION ===")
        Log.d("HistoryViewModel", "Input LocalDate: $newDate")

        // Usa UTC per evitare problemi di fuso orario
        val timestamp = newDate.atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        Log.d("HistoryViewModel", "Generated timestamp (UTC): $timestamp")

        // Verifica la conversione inversa
        val backToLocalDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        Log.d("HistoryViewModel", "Back to LocalDate: $backToLocalDate")
        Log.d("HistoryViewModel", "Dates match: ${newDate == backToLocalDate}")

        updateSelectedDate(timestamp)
    }

    /**
     * Ottiene la data selezionata come stringa formattata
     */
    fun getSelectedDateString(): String {
        return _selectedDate.value.toStringDate()
    }

    /*
        init {
            // Start collecting data when ViewModel is initialized
            viewModelScope.launch {
                updateSteps()
            }
            viewModelScope.launch {
                updateInfusion()
            }
            viewModelScope.launch {
                updateBleeding()
            }
        }
    */

    /*

        // Mutable state flow to hold the list of bleeding events
    private val _bleedingList: MutableStateFlow<List<BleedingEvent>> = MutableStateFlow(listOf())

    // Mutable state flow to hold the list of infusion events
    private val _infusionList: MutableStateFlow<List<InfusionEvent>> = MutableStateFlow(listOf())

    // Mutable state flow to hold the count of steps records
    private val _stepsCount: MutableStateFlow<Int> = MutableStateFlow(0)
     *
    /**
     * Update the steps count for the specified timestamp.
     *
     * @param newTimestamp The timestamp for which steps count needs to be updated.
     */
    suspend fun updateSteps(
        newTimestamp: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    ) {
        stepsRecordRepository.getAllDayStepsCount(newTimestamp).collect {
            _stepsCount.value = it
        }
    }

    /**
     * Update the list of infusion events for the specified timestamp.
     *
     * @param newTimestamp The timestamp for which infusion events need to be updated.
     */
    suspend fun updateInfusion(
        newTimestamp: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    ) {
        infusionRepository.getAllDayInfusion(newTimestamp).collect {
            _infusionList.value = it
        }
    }

    /**
     * Update the list of bleeding events for the specified timestamp.
     *
     * @param newTimestamp The timestamp for which bleeding events need to be updated.
     */
    suspend fun updateBleeding(
        newTimestamp: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    ) {
        bleedingRepository.getAllDayBleeding(newTimestamp).collect {
            _bleedingList.value = it
        }
    }
    */
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Represents the UI state for the history screen.
 *
 * @property bleedingList List of bleeding events.
 * @property infusionList List of infusion events.
 * @property stepsCount Count of steps records.
 */
data class HistoryUiState(
    val bleedingList: List<BleedingEvent> = listOf(),
    val infusionList: List<InfusionEvent> = listOf(),
    val prophylaxisList: List<ProphylaxisResponse> = listOf(),
    val stepsCount: Int = 0
) {
    val combinedEvents: List<BodyEvent>
        get() = (bleedingList.map { BodyEvent.Bleeding(it) } +
                infusionList.map { BodyEvent.Infusion(it) } +
                prophylaxisList.map { BodyEvent.Prophylaxis(it) })
            .sortedByDescending { it.dateTime }
}