package com.tempo.tempoapp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@SuppressLint("RestrictedApi")
class AlarmReceiver(
    private val alarmManager: AlarmManager = TempoApplication.instance.alarm,
    private val reminderRepository: ReminderRepository = TempoApplication.instance.container.reminderRepository
) :
    BroadcastReceiver() {


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager


        val event = intent?.getSerializable("REMINDER", ReminderEvent::class.java)
        val id = intent?.getLongExtra("id", 0)
        print("id $id")
        println(event)
        val title = "Reminder"
        val notification = NotificationCompat.Builder(context, "Reminder")
            .setContentTitle(title)
            .setTicker(event!!.event)
            .setContentText(event.event)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(false)
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
            val intent1 = Intent(context, AlarmReceiver::class.java)
            intent1.putExtra("id", id)
            intent1.putExtra("REMINDER", newEvent)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                newEvent.timestamp.toInt(),
                intent1,
                PendingIntent.FLAG_MUTABLE
            )
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    newEvent.timestamp,
                    pendingIntent
                )
        }


    }
}


@Suppress("UNCHECKED_CAST")
fun <T : Serializable?> Intent.getSerializable(key: String, mClass: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, mClass)!!
    else
        this.getSerializableExtra(key) as T
}