package com.tempo.tempoapp.ui.reminders

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import com.tempo.tempoapp.utils.AlarmManagerHelper
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * ViewModel class for the Reminder screen. Manages the UI state and handles business logic
 * related to creating and saving reminders.
 *
 * @param reminderRepository Repository for handling reminder events.
 * @param application The application context.
 */
class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
    application: Application
) : AndroidViewModel(application) {

    /**
     * Represents the current UI state of the reminder screen.
     */
    var uiState by mutableStateOf(ReminderUiState())
        private set

    /**
     * Updates the event in the UI state.
     *
     * @param event The new event value.
     */
    fun updateEvent(event: String) {
        uiState = uiState.copy(event = event)
    }

    /**
     * Updates the time in the UI state.
     *
     * @param time The new time value.
     */
    fun updateTime(time: String) {
        uiState = uiState.copy(
            time = time
        )
    }

    /**
     * Updates the date in the UI state.
     *
     * @param date The new date value.
     */
    fun updateDate(date: Long) {
        uiState = uiState.copy(
            date = date.toStringDate()
        )
    }

    /**
     * Updates the interval in the UI state.
     *
     * @param interval The new interval value.
     */
    fun updateInterval(interval: Int) {
        uiState = uiState.copy(
            interval = interval.toLong()
        )
    }

    /**
     * Updates whether the reminder is periodic in the UI state.
     *
     * @param isPeriodic The new value indicating whether the reminder is periodic.
     */
    fun updateIsPeriodic(isPeriodic: Boolean) {
        uiState = uiState.copy(
            isPeriodic = isPeriodic
        )
    }

    /**
     * Updates the time unit in the UI state.
     *
     * @param timeUnit The new time unit value.
     */
    fun updateTimeUnit(timeUnit: TimeUnit) {
        uiState = uiState.copy(
            timeUnit = timeUnit
        )
    }

    /**
     * Resets the UI state to its initial values.
     */
    fun reset() {
        uiState = ReminderUiState()
    }


    /**
     * Saves the reminder.
     */
    suspend fun save() {
        var data = uiState.toReminderEvent(-1)
        val idCalendar = createEvent(getApplication<Application>().applicationContext, data)
        println(idCalendar)
        reminderRepository.insertItem(data.copy(idCalendar = idCalendar))
        data = data.copy(idCalendar = idCalendar)
        AlarmManagerHelper(getApplication<Application>().applicationContext).scheduleReminderService(
            data
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

/**
 * Creates a calendar event based on the provided reminder event data.
 *
 * @param context The application context.
 * @param reminderEvent The reminder event data.
 * @return The ID of the created calendar event.
 */
private fun createEvent(
    context: Context,
    reminderEvent: ReminderEvent
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

/**
 * Retrieves the ID of the default calendar.
 *
 * @param context The application context.
 * @return The ID of the default calendar.
 */
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


/**
 * Represents the UI state of the reminder screen.
 *
 * @property event The reminder event.
 * @property date The reminder date.
 * @property time The reminder time.
 * @property isPeriodic Indicates whether the reminder is periodic.
 * @property interval The reminder interval.
 * @property timeUnit The time unit for the reminder interval.
 */
data class ReminderUiState(
    val event: String = "Tipo di evento",
    val date: String = Instant.now().toEpochMilli().toStringDate(),
    val time: String = Instant.now().toEpochMilli().toStringTime(),
    val isPeriodic: Boolean = false,
    val interval: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.DAYS
)

/**
 * Converts the UI state of a reminder to a ReminderEvent.
 *
 * @param idCalendar The ID of the calendar associated with the reminder.
 * @return The converted ReminderEvent.
 */
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
        ).parse(date.plus(" $time"))?.time ?: Instant.now().toEpochMilli()
    )
