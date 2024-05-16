package com.tempo.tempoapp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.repository.ReminderRepository
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

@SuppressLint("RestrictedApi")
class StepsReceiver(
    private val alarmManager: AlarmManager = TempoApplication.instance.alarm,
    private val reminderRepository: ReminderRepository = TempoApplication.instance.container.reminderRepository
) :
    BroadcastReceiver() {


    //@RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, StepsService::class.java)
        context.startForegroundService(serviceIntent)
        val instant = intent?.getLongExtra("instant", Instant.now().toEpochMilli())
        val newInstant = Instant.ofEpochMilli(instant!!).plus(30, ChronoUnit.MINUTES).toEpochMilli()
        val intent1 = Intent(context, StepsReceiver::class.java)
        intent1.putExtra(
            "instant",
            newInstant
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            newInstant.toInt(),
            intent1,
            PendingIntent.FLAG_MUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    newInstant,
                    pendingIntent
                )
        } else
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newInstant,
                pendingIntent
            )

    }
}


@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T : Serializable?> Intent.getSerializable(key: String, mClass: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, mClass)!!
    else
        this.getSerializableExtra(key) as T
}