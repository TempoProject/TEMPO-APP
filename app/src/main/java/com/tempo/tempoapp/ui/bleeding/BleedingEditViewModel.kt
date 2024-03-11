package com.tempo.tempoapp.ui.bleeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.BleedingRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BleedingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val bleedingRepository: BleedingRepository
) : ViewModel() {

    private val eventId: Int =
        checkNotNull(savedStateHandle[BleedingEventEditDestination.itemIdArg])

    var uiState by mutableStateOf(BleedingEventUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = bleedingRepository.getItemFromId(eventId).filterNotNull().first()
                .toBleedingUiState()
        }
    }

    suspend fun update() {
        if (validateInput(uiState.bleedingDetails))
            println("valid")
            bleedingRepository.updateItem(uiState.bleedingDetails.toEntity())
    }

    fun updateUiState(details: BleedingDetails) {
        uiState = BleedingEventUiState(
            details, validateInput(details)
        )
    }

    private fun validateInput(bleedingDetails: BleedingDetails = uiState.bleedingDetails): Boolean {
        return with(bleedingDetails) {
            site.isNotBlank()
                    && cause.isNotBlank()
                    && painScale.isNotBlank()
                    //&& severity.isNotBlank()
                    && date.isNotBlank()
                    && time.isNotBlank()

        }
    }
}

