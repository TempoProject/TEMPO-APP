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

class HistoryViewModel(
    private val bleedingRepository: BleedingRepository,
    private val infusionRepository: InfusionRepository,
    private val stepsRecordRepository: StepsRecordRepository,
) : ViewModel() {

    private val _bleedingList: MutableStateFlow<List<BleedingEvent>> = MutableStateFlow(listOf())

    private val _infusionList: MutableStateFlow<List<InfusionEvent>> = MutableStateFlow(listOf())

    private val _stepsCount: MutableStateFlow<Int> = MutableStateFlow(0)

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

    suspend fun updateSteps(
        newTimestamp: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    ) {
        stepsRecordRepository.getAllDayStepsCount(newTimestamp).collect {
            _stepsCount.value = it
        }
    }

    suspend fun updateInfusion(
        newTimestamp: Long = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
    ) {
        infusionRepository.getAllDayInfusion(newTimestamp).collect {
            _infusionList.value = it
        }
    }

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

data class HistoryUiState(
    val bleedingList: List<BleedingEvent> = listOf(),
    val infusionList: List<InfusionEvent> = mutableListOf(),
    val stepsCount: Int = 0
)