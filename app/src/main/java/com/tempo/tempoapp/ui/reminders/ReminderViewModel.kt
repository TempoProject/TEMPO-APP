package com.tempo.tempoapp.ui.reminders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import com.tempo.tempoapp.workers.NotificationWorker
import java.text.SimpleDateFormat
import java.time.Duration
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

    fun updateInterval(interval: Int) {
        uiState = uiState.copy(
            interval = interval.toLong()
        )
    }

    fun updateIsPeriodic(isPeriodic: Boolean) {
        uiState = uiState.copy(
            isPeriodic = isPeriodic
        )
    }

    fun updateTimeUnit(timeUnit: TimeUnit) {
        uiState = uiState.copy(
            timeUnit = timeUnit
        )
    }

    fun reset() {
        uiState = ReminderUiState()
    }


    suspend fun save() {
        val uuid = UUID.randomUUID()
        val data = uiState.toReminderEvent(uuid)

        if (uiState.isPeriodic) {
            val task = PeriodicWorkRequest.Builder(
                NotificationWorker::class.java,
                uiState.interval,
                uiState.timeUnit
            ).setInitialDelay(Duration.between(Instant.now(), Instant.ofEpochMilli(data.timestamp)))
                .setId(uuid)
                .setInputData(Data.Builder().putString("EVENT", uiState.event).build())
                .build()
            workManager.enqueueUniquePeriodicWork(
                uuid.toString(),
                ExistingPeriodicWorkPolicy.KEEP,
                task
            )
        } else {
            val task = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInputData(Data.Builder().putString("EVENT", uiState.event).build())
                .setInitialDelay(
                    Duration.between(
                        Instant.now(),
                        Instant.ofEpochMilli(data.timestamp)
                    )
                )
                .setId(uuid)
                .build()
            workManager.enqueue(task)
        }
        println(uuid)
        reminderRepository.insertItem(data)
    }
}

data class ReminderUiState(
    val event: String = "Tipo di evento",
    val date: String = Instant.now().toEpochMilli().toStringDate(),
    val time: String = Instant.now().toEpochMilli().toStringTime(),
    val isPeriodic: Boolean = false,
    val interval: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.DAYS
)

fun ReminderUiState.toReminderEvent(uuid: UUID): ReminderEvent =
    ReminderEvent(
        event = event,
        uuid = uuid,
        isPeriodic = isPeriodic,
        period = interval,
        timeUnit = timeUnit.name,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.plus(" $time")).time
    )
