package com.tempo.tempoapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    bleedingRepository: BleedingRepository,
    infusionRepository: InfusionRepository
) : ViewModel() {


    val homeUiState: StateFlow<HomeUiState> =
        bleedingRepository.getAll().combine(infusionRepository.getAll()) { bleeding, infusion ->
            HomeUiState(bleeding, infusion)
        }.stateIn(
            scope = viewModelScope,
            initialValue = HomeUiState(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(
    val bleedingList: List<BleedingEvent> = listOf(),
    val infusionList: List<InfusionEvent> = mutableListOf()
)