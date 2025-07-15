package com.tempo.tempoapp.ui.prophylaxis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.data.repository.ProphylaxisResponseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProphylaxisDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val prophylaxisRepository: ProphylaxisResponseRepository
) : ViewModel() {

    private val itemId: Int =
        checkNotNull(savedStateHandle[ProphylaxisDetailsScreenRoute.itemIdArg])


    val uiState: StateFlow<ProphylaxisDetailsUiState> =
        prophylaxisRepository.getItemFromId(itemId)
            .filterNotNull()
            .map { prophylaxisResponse ->
                ProphylaxisDetailsUiState(
                    prophylaxisDetails = prophylaxisResponse.toProphylaxisDetails(),
                    id = prophylaxisResponse.id,
                    isLoading = false
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ProphylaxisDetailsUiState(isLoading = true)
            )

    suspend fun deleteItem(): Boolean {
        return try {
            val currentState = uiState.value
            if (!currentState.isLoading && currentState.id != -1) {
                prophylaxisRepository.deleteItem(currentState.prophylaxisDetails.toEntity())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ProphylaxisDetailsUiState(
    val prophylaxisDetails: ProphylaxisDetails = ProphylaxisDetails(),
    val id: Int = -1,
    val isLoading: Boolean = true
)

fun ProphylaxisResponse.toProphylaxisDetails(): ProphylaxisDetails {
    return ProphylaxisDetails(
        id = this.id,
        reminderDateTime = this.reminderDateTime,
        responded = this.responded,
        responseDateTime = this.responseDateTime,
        date = this.date,
        reminderType = this.reminderType,
        drugName = this.drugName,
        dosage = this.dosage,
        dosageUnit = this.dosageUnit
    )
}

fun ProphylaxisDetails.toEntity(): ProphylaxisResponse {
    return ProphylaxisResponse(
        id = this.id,
        reminderDateTime = this.reminderDateTime,
        responded = this.responded,
        responseDateTime = this.responseDateTime,
        date = this.date,
        reminderType = this.reminderType,
        drugName = this.drugName,
        dosage = this.dosage,
        dosageUnit = this.dosageUnit
    )
}

data class ProphylaxisDetails(
    val id: Int = -1,
    val reminderDateTime: Long = -1,
    val responded: Int = -1,
    val responseDateTime: Long = -1,
    val date: Long = -1,
    val reminderType: String = "",
    val drugName: String = "",
    val dosage: String = "",
    val dosageUnit: String = "",
)