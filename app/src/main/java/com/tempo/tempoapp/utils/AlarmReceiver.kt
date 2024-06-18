package com.tempo.tempoapp.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tempo.tempoapp.MainActivity
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.ReminderEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * AlarmReceiver is a BroadcastReceiver responsible for handling alarm events triggered by reminders.
 * It receives broadcasts when reminder alarms go off and displays notifications accordingly.
 * If the reminder is periodic, it reschedules the alarm for the next occurrence.
 *
 * @property alarmManager The AlarmManager instance used to schedule alarms.
 * @property reminderRepository The repository for managing reminder data.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }

    /**
     * Handles the action to be taken when a reminder alarm is received.
     *
     * @param context The context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderRepository =
            (context?.applicationContext as TempoApplication).container.reminderRepository
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager


        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val event = intent?.getSerializable("REMINDER", ReminderEvent::class.java)
        val id = intent?.getIntExtra("id", 0)
        println("id $id")
        println(event)
        val title = "Reminder"
        val notification = NotificationCompat.Builder(
            context,
            context.getString(R.string.reminder_notification_channel_id)
        )
            .setContentTitle(title)
            .setTicker(event!!.event)
            .setContentText(event.event)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(notificationPendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.Default.nextInt(), notification)

        if (event.isPeriodic) {
            val temporalUnit = when (event.timeUnit) {
                "HOURS" -> ChronoUnit.HOURS
                "DAYS" -> ChronoUnit.DAYS
                else -> ChronoUnit.HOURS
            }
            val newEvent = event.copy(
                id = id!!.toInt(),
                timestamp = Instant.ofEpochMilli(event.timestamp)
                    .plus(event.period, temporalUnit).toEpochMilli()
            )
            CoroutineScope(IO).launch {
                reminderRepository.updateItem(newEvent)
            }

            Log.d(TAG, "Rescheduling alarm for ${newEvent.timestamp}")
            AlarmManagerHelper(context).scheduleReminderService(newEvent)
        }
    }
}

