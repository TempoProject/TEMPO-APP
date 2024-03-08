package com.tempo.tempoapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val bleedingRepository: BleedingRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        bleedingRepository.getAll().map { HomeUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = HomeUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(val itemList: List<BleedingEvent> = listOf())