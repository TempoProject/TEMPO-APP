package com.tempo.tempoapp.ui.reminders

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
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


    suspend fun save() {
        //val uuid = UUID.randomUUID()
        val data = uiState.toReminderEvent(-1)
        val idCalendar = createEvent(context, data)
        print(idCalendar)
        reminderRepository.insertItem(data.copy(idCalendar = idCalendar))
        /*
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

         */
    }
}

private fun createEvent(
    context: Context,
    reminderEvent: ReminderEvent
    /*
    title: String,
    description: String,
    startTimeMillis: Long,
    endTimeMillis: Long

     */
): Long {
    val eventValues = ContentValues().apply {
        put(CalendarContract.Events.TITLE, reminderEvent.event)
        put(CalendarContract.Events.DESCRIPTION, reminderEvent.event)
        put(CalendarContract.Events.DTSTART, reminderEvent.timestamp)
        put(CalendarContract.Events.DTEND, reminderEvent.timestamp)
        if (reminderEvent.isPeriodic) {
            println(reminderEvent.timeUnit)
            val freq = when (reminderEvent.timeUnit) {
                "DAYS" -> "DAILY"
                "HOURS" -> "HOURLY"
                else -> ""
            }
            put(CalendarContract.Events.RRULE, "FREQ=$freq;INTERVAL=${reminderEvent.period}")
        }
        put(
            CalendarContract.Events.CALENDAR_ID,
            getCalendarId(context)
        ) // Using the default calendar ID
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }
    val contentResolver = context.contentResolver
    val id = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)

    val idLong = id?.lastPathSegment?.toLong()

    idLong.let {
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, it)
            put(CalendarContract.Reminders.MINUTES, 5)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
    }
    return id?.lastPathSegment?.toLong() ?: -1

}

private fun getCalendarId(context: Context): Long {
    var calendarId: Long = -1

    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME
    )

    val selection = "${CalendarContract.Calendars.IS_PRIMARY} = ?"
    val selectionArgs = arrayOf("1") // "1" indicates true

    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use { cursor ->
        val idColumnIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
        val idColumnName = cursor.getColumnIndex(CalendarContract.Calendars.NAME)
        while (cursor.moveToNext()) {
            calendarId = cursor.getLong(idColumnIndex)
            Log.d("ReminderScreen", cursor.getString(idColumnName))
            break
        }
    }

    return calendarId
}

data class ReminderUiState(
    val event: String = "Tipo di evento",
    val date: String = Instant.now().toEpochMilli().toStringDate(),
    val time: String = Instant.now().toEpochMilli().toStringTime(),
    val isPeriodic: Boolean = false,
    val interval: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.DAYS
)

fun ReminderUiState.toReminderEvent(idCalendar: Long): ReminderEvent =
    ReminderEvent(
        event = event,
        idCalendar = idCalendar,
        isPeriodic = isPeriodic,
        period = interval,
        timeUnit = timeUnit.name,
        timestamp = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).parse(date.plus(" $time")).time
    )
