package com.tempo.tempoapp.ui.movesense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.repository.MovesenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MovesenseViewModel(private val movesenseRepository: MovesenseRepository) : ViewModel() {


    private val _movesenseUiState =
        MutableStateFlow(MovesenseUiState(Movesense("", "", false)))
    val movesenseUiState: StateFlow<MovesenseUiState> =
        combine(
            movesenseRepository.getDevice(),
            _movesenseUiState
        ) { movesense, uiState ->
            var device = Movesense("", "", false)
            if (movesense != null) device = movesense
            uiState.copy(movesense = device)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _movesenseUiState.value
        )

    suspend fun updateInfoDevice(item: Movesense) {
        movesenseRepository.updateItem(item)
    }

    suspend fun deleteInfoDevice(item: Movesense) {
        movesenseRepository.deleteItem(item)
    }

    fun updateUi(isWorking: Boolean = false) {
        _movesenseUiState.update {
            it.copy(
                movesense = it.movesense,
                isWorking = isWorking
            )
        }
    }

}

data class MovesenseUiState(
    val movesense: Movesense,
    val isWorking: Boolean = false,
)