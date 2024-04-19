package com.tempo.tempoapp.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import com.tempo.tempoapp.utils.AlarmReceiver
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
    private val workManager: WorkManager,
    private val alarmManager: AlarmManager,
    private val context: Context
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


    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun save() {
        val uuid = UUID.randomUUID()
        val data = uiState.toReminderEvent(uuid)
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("REMINDER", data)


        //println("istant ${Insta}")
        println("istant plus 1hr: ${Instant.now().plus(1, ChronoUnit.HOURS)}")
        /* if (uiState.isPeriodic) {
             val intervalMillis = when (data.timeUnit) {
                 TimeUnit.DAYS.name -> AlarmManager.INTERVAL_DAY
                 TimeUnit.HOURS.name -> AlarmManager.INTERVAL_HOUR
                 else -> {
                     0
                 }
             }
             alarmManager.setRepeating(
                 AlarmManager.RTC_WAKEUP,
                 data.timestamp,
                 data.period * intervalMillis,
                 pendingIntent
             )
             /*
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
                 ExistingPeriodicWorkPolicy.UPDATE,
                 task
             )*/
         } else */

        println(uuid)
        intent.putExtra("id", reminderRepository.insertItem(data))
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            data.timestamp.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        if (alarmManager.canScheduleExactAlarms())
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                data.timestamp,
                pendingIntent
            )
        /*
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
         */


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
