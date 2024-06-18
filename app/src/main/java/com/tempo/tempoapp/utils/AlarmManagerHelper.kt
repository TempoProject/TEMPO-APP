package com.tempo.tempoapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tempo.tempoapp.data.model.ReminderEvent

/**
 * AlarmManagerHelper is a helper class that provides methods for scheduling alarms using AlarmManager.
 *
 * @param context The context in which the AlarmManager is used.
 */
class AlarmManagerHelper(val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a stepService to be executed at a specific time.
     *
     * @param pendingIntent The PendingIntent to be executed.
     * @param newInstant The time at which the PendingIntent should be executed.
     */
    fun scheduleStepsService(pendingIntent: PendingIntent, newInstant: Long) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    newInstant,
                    pendingIntent
                )
            }
        } else
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newInstant,
                pendingIntent
            )
    }

    /**
     * Schedules a reminder event to be executed at a specific time.
     *
     * @param data The reminder event to be scheduled.
     */
    fun scheduleReminderService(data: ReminderEvent) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("id", data.id)
        intent.putExtra("REMINDER", data)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            data.timestamp.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    data.timestamp,
                    pendingIntent
                )
        } else
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                data.timestamp,
                pendingIntent
            )

    }
}