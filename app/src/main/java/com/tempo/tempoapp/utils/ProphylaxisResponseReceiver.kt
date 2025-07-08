package com.tempo.tempoapp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class ProphylaxisResponseReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_YES_RESPONSE = "com.tempo.ACTION_YES_RESPONSE"
        const val ACTION_NO_RESPONSE = "com.tempo.ACTION_NO_RESPONSE"
        const val ACTION_POSTPONE_RESPONSE = "com.tempo.ACTION_POSTPONE_RESPONSE"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_ID_RESPONSE = "id_response"
        const val EXTRA_REMINDER_DATETIME = "reminder_datetime"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_DRUG_NAME = "drug_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_DOSAGE_UNITS = "dosage_units"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_YES_RESPONSE -> handleResponse(context, true, intent)
            ACTION_NO_RESPONSE -> handleResponse(context, false, intent)
            ACTION_POSTPONE_RESPONSE -> handlePostpone(context, intent)
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    private fun handleResponse(context: Context, responded: Boolean, intent: Intent) {
        val idResponse = intent.getLongExtra(EXTRA_ID_RESPONSE, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val reminderDateTime =
            intent.getLongExtra(EXTRA_REMINDER_DATETIME, System.currentTimeMillis())
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "unknown"
        val drugName = intent.getStringExtra(EXTRA_DRUG_NAME) ?: ""
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val dosageUnit = intent.getStringExtra(EXTRA_DOSAGE_UNITS) ?: ""

        try {
            // BUG notificationID
            NotificationManagerCompat.from(context).cancelAll()
            Log.d("ResponseReceiver", "Notifica $notificationId cancellata")
        } catch (e: Exception) {
            Log.e("ResponseReceiver", "Errore nella cancellazione notifica", e)
        }

        val prophylaxisResponseRepository =
            (context.applicationContext as TempoApplication).container.prophylaxisResponseRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ProphylaxisResponse(
                    id = idResponse.toInt(),
                    reminderDateTime = reminderDateTime,
                    responded = responded.toInt(),
                    responseDateTime = System.currentTimeMillis(),
                    reminderType = reminderType,
                    drugName = drugName,
                    dosage = dosage,
                    dosageUnit = dosageUnit,
                    date = Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                        ChronoUnit.DAYS
                    ).toEpochMilli()
                )

                prophylaxisResponseRepository.updateItem(response)
            } catch (e: Exception) {
                Log.e("ResponseReceiver", "Errore nel salvare la risposta", e)
            }
        }
    }

    private fun handlePostpone(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val idResponse = intent.getLongExtra(EXTRA_ID_RESPONSE, -1L)
        /*val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "unknown"
        val drugName = intent.getStringExtra(EXTRA_DRUG_NAME) ?: ""
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val dosageUnit = intent.getStringExtra(EXTRA_DOSAGE_UNITS) ?: ""*/

        try {
            // Cancella la notifica corrente
            NotificationManagerCompat.from(context).cancelAll()
            Log.d("ResponseReceiver", "Notifica $notificationId cancellata per posticipo")
        } catch (e: Exception) {
            Log.e("ResponseReceiver", "Errore nella cancellazione notifica per posticipo", e)
        }

        schedulePostponedNotification(
            context = context,
            originalResponseId = idResponse,
        )
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun schedulePostponedNotification(
        context: Context,
        originalResponseId: Long,
    ) {
        val postponeDelayMillis = 30 * 60 * 1000L
        val postponedTriggerTime = System.currentTimeMillis() + postponeDelayMillis


        val postponedAlarmId = 9000 + Random.nextInt(1000)

        val intent = Intent(context, ProphylaxisAlarmReceiver::class.java).apply {
            putExtra("title", context.getString(R.string.prophylaxis_reminder))
            putExtra("message", context.getString(R.string.prophylaxis_time))
            putExtra("alarmId", postponedAlarmId)
            putExtra("originalResponseId", originalResponseId)
            putExtra("isPostponed", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            postponedAlarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                postponedTriggerTime,
                pendingIntent
            )
            Log.d("ResponseReceiver", "Notifica posticipata programmata per ${java.util.Date(postponedTriggerTime)}")
        } catch (e: Exception) {
            Log.e("ResponseReceiver", "Errore nella programmazione della notifica posticipata", e)
        }
    }
}