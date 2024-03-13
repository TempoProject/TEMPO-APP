package com.tempo.tempoapp.ui.bleeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.BleedingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BleedingDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val bleedingRepository: BleedingRepository
) : ViewModel() {
    private val itemId: Int =
        checkNotNull(savedStateHandle[BleedingEventDetailsDestination.itemIdArg])

    val uiState: StateFlow<BleedingDetailsUiState> =
        bleedingRepository.getItemFromId(itemId).filterNotNull().map {
            BleedingDetailsUiState(bleedingDetails = it.toBleedingDetails(), itemId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = BleedingDetailsUiState()
        )

    suspend fun deleteItem() {
        bleedingRepository.deleteItem(uiState.value.bleedingDetails.toEntity())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

data class BleedingDetailsUiState(
    val bleedingDetails: BleedingDetails = BleedingDetails(),
    val id: Int = -1
)