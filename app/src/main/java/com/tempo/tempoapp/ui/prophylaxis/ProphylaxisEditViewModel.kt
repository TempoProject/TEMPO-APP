package com.tempo.tempoapp.ui.prophylaxis

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.repository.ProphylaxisResponseRepository
import com.tempo.tempoapp.utils.ProphylaxisAlarmReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class ProphylaxisEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val prophylaxisRepository: ProphylaxisResponseRepository
) : ViewModel() {

    private val itemId: Int =
        checkNotNull(savedStateHandle[ProphylaxisEditDestination.itemIdArg])

    var uiState by mutableStateOf(ProphylaxisEditUiState(isLoading = true))
        private set

    private var originalResponseDateTime: Long = -1L

    init {
        viewModelScope.launch {
            try {
                val prophylaxis = prophylaxisRepository.getItemFromId(itemId).first()

                if (originalResponseDateTime == -1L)
                    originalResponseDateTime = prophylaxis.responseDateTime

                uiState = ProphylaxisEditUiState(
                    prophylaxisDetails = prophylaxis.toProphylaxisDetails(),
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.d(
                    "ProphylaxisEditViewModel",
                    "Error fetching prophylaxis details: ${e.message}"
                )
                uiState = ProphylaxisEditUiState(
                    isLoading = false
                )
            }
        }
    }

    fun getOriginalResponseDateTime(): Long = originalResponseDateTime


    fun updateUiState(prophylaxisDetails: ProphylaxisDetails) {

        if (prophylaxisDetails.responded == 0 && uiState.prophylaxisDetails.responded == 1) {
            prophylaxisDetails.copy(date = -1)
        } else if (prophylaxisDetails.responded == 1 && uiState.prophylaxisDetails.responded != 1) {
            if (prophylaxisDetails.date == -1L) {
                prophylaxisDetails.copy(
                    responseDateTime = Instant.now()
                        .truncatedTo(ChronoUnit.MILLIS).toEpochMilli()
                )
            } else {
                prophylaxisDetails
            }
        } else {
            prophylaxisDetails
        }


        uiState = uiState.copy(
            prophylaxisDetails = prophylaxisDetails,
        )
    }


    fun updateProphylaxisResponse(
        context: Context
    ): Boolean {
        return try {
            viewModelScope.launch {
                if (uiState.prophylaxisDetails.responded == 0) {
                    updateUiState(uiState.prophylaxisDetails.copy(responseDateTime = 0L))
                }

                cancelPostponedAlarmIfExists(context, uiState.prophylaxisDetails.id)
                prophylaxisRepository.updateItem(uiState.prophylaxisDetails.toEntity())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun cancelPostponedAlarmIfExists(context: Context, responseId: Int) {
        try {
            val prophylaxisRepository =
                (context.applicationContext as TempoApplication).container.prophylaxisResponseRepository
            val currentResponse = prophylaxisRepository.getItemFromId(responseId).first()
            currentResponse.postponedAlarmId.let { alarmId ->
                if (alarmId != -1) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(context, ProphylaxisAlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        alarmId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)

                    prophylaxisRepository.updatePostponedAlarmId(responseId.toLong(), -1)

                    Log.d("ProphylaxisEditScreen", "Alarm posticipato $alarmId cancellato")
                }
            }
        } catch (e: Exception) {
            Log.e("ProphylaxisEditScreen", "Errore nella cancellazione dell'alarm posticipato", e)
        }
    }
}

data class ProphylaxisEditUiState(
    val prophylaxisDetails: ProphylaxisDetails = ProphylaxisDetails(),
    val responded: Int = -1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)