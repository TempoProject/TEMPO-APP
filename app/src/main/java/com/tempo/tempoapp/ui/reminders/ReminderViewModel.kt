package com.tempo.tempoapp.ui.reminders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import com.tempo.tempoapp.workers.NotificationWorker
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
    private val workManager: WorkManager
) : ViewModel() {

    var uiState by mutableStateOf(ReminderUiState())
        private set


    fun updateEvent(event: String) {
        uiState = uiState.copy(event = event)
    }

    fun updateTime(time: String) {
        uiState = uiState.copy(
            time = time
        )
    }

    fun updateDate(date: Long) {
        uiState = uiState.copy(
            date = date.toStringDate()
        )
    }

    fun reset() {
        uiState = ReminderUiState()
    }


    suspend fun save() {
        /*
        TODO:
          - fix initial delay per schedulare la notifica all'orario corretto.
          - aggiungere un parametro per schedulare notifiche periodiche.
         */
        val task = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(Data.Builder().putString("EVENT", uiState.event).build())
            .setInitialDelay(1, TimeUnit.MINUTES).build()
        workManager.enqueue(task)
        val data = uiState.toReminderEvent(task.id)
        reminderRepository.insertItem(data)
    }
}

data class ReminderUiState(
    val event: String = "Tipo di evento",
    val date: String = Instant.now().toEpochMilli().toStringDate(),
    val time: String = Instant.now().toEpochMilli().toStringTime()
)

fun ReminderUiState.toReminderEvent(uuid: UUID): ReminderEvent =
    ReminderEvent(
        event = event,
        uuid = uuid,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.plus(" $time")).time
    )
