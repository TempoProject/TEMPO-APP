package com.tempo.tempoapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.time.Instant

class RebootBroadcastReceiver(
    private val reminderRepository: ReminderRepository = TempoApplication.instance.container.reminderRepository,
    private val alarmManager: AlarmManager = TempoApplication.instance.alarm
) :
    BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action == "android.intent.action.BOOT_COMPLETED") {
            CoroutineScope(Main).launch {
                val intent = Intent(p0!!, StepsReceiver::class.java)
                val instant = Instant.now().toEpochMilli()
                intent.putExtra("instant", instant)
                val pendingIntent = PendingIntent.getBroadcast(
                    p0,
                    instant.toInt(),
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms())
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            instant,
                            pendingIntent
                        )
                } else
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        instant,
                        pendingIntent
                    )
                reminderRepository.getAll().collect { reminders ->
                    for (reminder in reminders) {
                        val intent = Intent(p0, StepsReceiver::class.java)
                        intent.putExtra("id", reminder.id)
                        intent.putExtra("REMINDER", reminder)
                        val pendingIntent = PendingIntent.getBroadcast(
                            p0,
                            reminder.timestamp.toInt(),
                            intent,
                            PendingIntent.FLAG_MUTABLE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms())
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminder.timestamp,
                                    pendingIntent
                                )
                        } else
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminder.timestamp,
                                pendingIntent
                            )

                    }
                }
            }
        }
    }
}
