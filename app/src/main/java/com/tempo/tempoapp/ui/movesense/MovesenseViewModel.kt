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

/**
 * ViewModel class for managing the UI state and interactions related to Movesense devices.
 *
 * @param movesenseRepository Repository for handling Movesense device operations.
 */
class MovesenseViewModel(private val movesenseRepository: MovesenseRepository) : ViewModel() {


    // Internal MutableStateFlow to manage Movesense UI state
    private val _movesenseUiState =
        MutableStateFlow(MovesenseUiState(Movesense("", "", false)))

    /**
     * Publicly exposed StateFlow of the Movesense UI state.
     * Combines the state from the repository and internal state to provide a complete UI state.
     */
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

    /**
     * Updates the Movesense device information in the repository.
     *
     * @param item The Movesense device to be updated.
     */
    suspend fun updateInfoDevice(item: Movesense) {
        movesenseRepository.updateItem(item)
    }

    /**
     * Deletes the Movesense device information from the repository.
     *
     * @param item The Movesense device to be deleted.
     */
    suspend fun deleteInfoDevice(item: Movesense) {
        movesenseRepository.deleteItem(item)
    }

    /**
     * Updates the UI state to reflect whether a process involving the Movesense device is working.
     *
     * @param isWorking Boolean indicating whether the process is working.
     */
    fun updateUi(isWorking: Boolean = false) {
        _movesenseUiState.update {
            it.copy(
                movesense = it.movesense,
                isWorking = isWorking
            )
        }
    }

}

/**
 * Data class representing the UI state of the Movesense screen.
 *
 * @param movesense The Movesense device information.
 * @param isWorking Boolean indicating whether a process involving the Movesense device is working.
 */
data class MovesenseUiState(
    val movesense: Movesense,
    val isWorking: Boolean = false,
)