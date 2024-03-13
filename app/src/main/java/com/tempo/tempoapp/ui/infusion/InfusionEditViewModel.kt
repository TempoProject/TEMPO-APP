package com.tempo.tempoapp.ui.infusion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.repository.InfusionRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InfusionEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val infusionRepository: InfusionRepository
) : ViewModel() {

    private val eventId: Int =
        checkNotNull(savedStateHandle[InfusionEditDestination.itemIdArg])

    var uiState by mutableStateOf(InfusionUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = infusionRepository.getItemFromId(eventId).filterNotNull().first()
                .toInfusionUiState()
        }
    }

    suspend fun update() {
        if (validateInput())
            infusionRepository.updateItem(uiState.infusionDetails.toEntity())
    }

    fun updateUiState(infusionDetails: InfusionDetails) {
        uiState = InfusionUiState(infusionDetails, validateInput(infusionDetails))
    }

    private fun validateInput(infusionDetails: InfusionDetails = uiState.infusionDetails): Boolean {
        return with(infusionDetails) {
            treatment.isNotBlank() &&
                    infusionSite.isNotBlank() &&
                    doseUnits.isNotBlank() &&
                    lotNumber.isNotBlank() &&
                    date.isNotBlank() &&
                    time.isNotBlank()
        }
    }
}