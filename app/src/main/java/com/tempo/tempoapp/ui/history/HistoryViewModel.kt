package com.tempo.tempoapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
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
) : ViewModel() {

    // Mutable state flow to hold the list of bleeding events
    private val _bleedingList: MutableStateFlow<List<BleedingEvent>> = MutableStateFlow(listOf())

    // Mutable state flow to hold the list of infusion events
    private val _infusionList: MutableStateFlow<List<InfusionEvent>> = MutableStateFlow(listOf())

    // Mutable state flow to hold the count of steps records
    private val _stepsCount: MutableStateFlow<Int> = MutableStateFlow(0)

    // State flow to emit the UI state combining bleeding events, infusion events, and steps count
    val historyUiState: StateFlow<HistoryUiState> = combine(
        _bleedingList,
        _infusionList,
        _stepsCount
    ) { bleeding, infusion, stepsCount ->
        HistoryUiState(bleeding, infusion, stepsCount)
    }.stateIn(
        scope = viewModelScope,
        initialValue = HistoryUiState(),
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
    )

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
    val stepsCount: Int = 0
)