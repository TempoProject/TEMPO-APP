package com.tempo.tempoapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tempo.tempoapp.TempoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * RebootBroadcastReceiver is a BroadcastReceiver responsible for scheduling alarms
 * after the device is rebooted.
 *
 */

class RebootBroadcastReceiver :
    BroadcastReceiver() {

    companion object {
        private val TAG = RebootBroadcastReceiver::class.java.simpleName
    }

    /**
     * Receives the broadcast when the device is rebooted and schedules alarms for reminders.
     *
     * @param p0 The context in which the receiver is running.
     * @param p1 The Intent being received.
     */
    //@RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAG, "onReceive: ${p1?.action}")

        val alarmManager = p0?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderRepository =
            (p0.applicationContext as TempoApplication).container.reminderRepository

        if (p1?.action == "android.intent.action.BOOT_COMPLETED") {

            val intent = Intent(p0, StepsReceiver::class.java)
            val instant = Instant.now().toEpochMilli()
            intent.putExtra("instant", instant)
            val pendingIntent = PendingIntent.getBroadcast(
                p0,
                instant.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                 )*/
            Log.d(TAG, "scheduled steps service with instant: $instant")
            AlarmManagerHelper(p0).scheduleStepsService(pendingIntent, instant)
            CoroutineScope(Main).launch {

                Log.d(TAG, "scheduling alarms after reboot")
                reminderRepository.getAll().collect { reminders ->
                    for (reminder in reminders) {
                        val newIntent = Intent(p0, AlarmReceiver::class.java)
                        newIntent.putExtra("id", reminder.id)
                        newIntent.putExtra("REMINDER", reminder)
                        val newPendingIntent = PendingIntent.getBroadcast(
                            p0,
                            reminder.timestamp.toInt(),
                            newIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms())
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminder.timestamp,
                                    newPendingIntent
                                )
                        } else
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminder.timestamp,
                                newPendingIntent
                            )

                    }
                }
            }
        }
    }
}

