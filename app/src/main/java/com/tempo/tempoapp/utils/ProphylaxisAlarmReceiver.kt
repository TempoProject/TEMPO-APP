package com.tempo.tempoapp.utils

import AppPreferencesManager
import AppPreferencesManager.ProphylaxisConfig
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tempo.tempoapp.MainActivity
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.ui.onboarding.SchedulingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

class ProphylaxisAlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: context.getString(R.string.prophylaxis_reminder)
        val message = intent.getStringExtra("message") ?: context.getString(R.string.prophylaxis_time)
        val alarmId = intent.getIntExtra("alarmId", -1)

        Log.d("AlarmReceiver", "Ricevuto alarm ID $alarmId: title=$title, message=$message")

        CoroutineScope(Dispatchers.Default).launch {
            val notificationId = Random(100).nextInt()
            val appPreferencesManager = AppPreferencesManager(context)
            try {
                val config = appPreferencesManager.prophylaxisConfig.first()

                val reminderType = when (alarmId) {
                    1000 -> "Settimanale"
                    2000 -> "Ricorrente"
                    else -> "Generico"
                }


                val responseId = insertProphylaxisResponse(
                    context = context,
                    reminderDateTime = System.currentTimeMillis(),
                    reminderType = reminderType,
                    drugName = config?.drugName ?: "N/A",
                    dosage = config?.dosage ?: "N/A",
                    dosageUnits = config?.dosageUnit ?: "N/A"
                )

                Log.d("AlarmReceiver", "Risposta inserita con ID: $notificationId")

                showNotification(
                    context,
                    title,
                    message,
                    notificationId,
                    responseId,
                    reminderType,
                    drugName = config?.drugName ?: "N/A",
                    dosage = config?.dosage ?: "N/A",
                    dosageUnits = config?.dosageUnit ?: "N/A"
                )

                rescheduleAlarm(context)
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Errore durante il recupero della configurazione", e)
            }
        }
    }

    private suspend fun insertProphylaxisResponse(
        context: Context,
        reminderDateTime: Long,
        reminderType: String,
        drugName: String,
        dosage: String,
        dosageUnits: String
    ): Long = withContext(Dispatchers.IO) {
        val prophylaxisResponseRepository =
            (context.applicationContext as TempoApplication).container.prophylaxisResponseRepository

        return@withContext prophylaxisResponseRepository.insertItem(
            ProphylaxisResponse(
                reminderDateTime = reminderDateTime,
                reminderType = reminderType,
                drugName = drugName,
                dosage = dosage,
                dosageUnit = dosageUnits,
                responded = -1, // -1 = non ancora risposto, 0 = No, 1 = Sì
                responseDateTime = 0L, // Sarà aggiornato quando l'utente risponde
                date = Instant.now().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(
                    ChronoUnit.DAYS
                ).toEpochMilli()
            )
        )
    }

    private fun rescheduleAlarm(context: Context) {
        val appPreferencesManager = AppPreferencesManager(context)
        val scheduler = ProphylaxisScheduler(context)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val config = appPreferencesManager.prophylaxisConfig.first()
                Log.d("AlarmReceiver", "Configurazione ricevuta per re-scheduling: $config")

                config?.let {
                    when (it.schedulingMode) {
                        SchedulingMode.DaysOfWeek -> {
                            // Per allarmi settimanali, re-schedula il giorno specifico
                            rescheduleWeeklyAlarm(context, it)
                        }

                        SchedulingMode.Recurring -> {
                            // Per allarmi ricorrenti, schedula il prossimo evento
                            rescheduleRecurringAlarm(scheduler, it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Errore durante il re-scheduling", e)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleWeeklyAlarm(
        context: Context,
        config: ProphylaxisConfig
    ) {

        config.selectedDays?.let { dayToReschedule ->
            Log.d("AlarmReceiver", "Re-scheduling per $dayToReschedule")

            val nextTrigger = LocalDateTime.now()
                .with(TemporalAdjusters.next(dayToReschedule))
                .withHour(config.hour)
                .withMinute(config.minute)
                .withSecond(0)
                .withNano(0)

            val millis = nextTrigger.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()

            val intent = Intent(context, ProphylaxisAlarmReceiver::class.java).apply {
                putExtra("title", context.getString(R.string.weekly_prophylaxis))
                putExtra(
                    "message",
                    "${context.getString(R.string.prophylaxis_time_notification)} ${
                        context.getString(getDayName(dayToReschedule))
                    }"
                )
                putExtra("alarmId", 1000) // ID fisso per singolo giorno
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1000, // ID fisso per singolo giorno
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
        }
    }

    private fun rescheduleRecurringAlarm(
        scheduler: ProphylaxisScheduler,
        config: ProphylaxisConfig
    ) {
        Log.d(
            "AlarmReceiver",
            "Re-scheduling ricorrente ogni ${config.recurrenceInterval} ${config.recurrenceUnit}"
        )

        scheduler.scheduleRecurringAlarm(
            interval = config.recurrenceInterval,
            unit = config.recurrenceUnit,
            startDate = config.startDate ?: LocalDate.now(),
            hour = config.hour,
            minute = config.minute
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        context: Context, title: String, message: String,
        notificationId: Int, responseId: Long, reminderType: String,
        drugName: String, dosage: String, dosageUnits: String
    ) {
        Log.d("AlarmReceiver", "Mostro notifica: $title - $message")

        val currentTimeMillis = System.currentTimeMillis()

        val yesIntent = Intent(context, ProphylaxisResponseReceiver::class.java).apply {
            action = ProphylaxisResponseReceiver.ACTION_YES_RESPONSE
            putExtra(ProphylaxisResponseReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ProphylaxisResponseReceiver.EXTRA_ID_RESPONSE, responseId)
            putExtra(ProphylaxisResponseReceiver.EXTRA_REMINDER_DATETIME, currentTimeMillis)
            putExtra(ProphylaxisResponseReceiver.EXTRA_REMINDER_TYPE, reminderType)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DRUG_NAME, drugName)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DOSAGE, dosage)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DOSAGE_UNITS, dosageUnits)
        }

        val yesPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1, // ID univoco per evitare conflitti
            yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val noIntent = Intent(context, ProphylaxisResponseReceiver::class.java).apply {
            action = ProphylaxisResponseReceiver.ACTION_NO_RESPONSE
            putExtra(ProphylaxisResponseReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ProphylaxisResponseReceiver.EXTRA_ID_RESPONSE, responseId)
            putExtra(ProphylaxisResponseReceiver.EXTRA_REMINDER_DATETIME, currentTimeMillis)
            putExtra(ProphylaxisResponseReceiver.EXTRA_REMINDER_TYPE, reminderType)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DRUG_NAME, drugName)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DOSAGE, dosage)
            putExtra(ProphylaxisResponseReceiver.EXTRA_DOSAGE_UNITS, dosageUnits)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2, // ID univoco per evitare conflitti
            noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val detailedMessage = buildString {
            append(context.getString(R.string.did_you_treat_yself))
            if (drugName.isNotBlank()) {
                append(" con $drugName")
                if (dosage.isNotBlank()) {
                    append(" ($dosage)")
                }
            }
            append("?")
        }

        val builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.reminder_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.prophylaxis_reminder))
            .setContentText(detailedMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                context.getString(R.string.yes),
                yesPendingIntent
            )
            .addAction(
                R.drawable.ic_close,
                context.getString(R.string.no),
                noPendingIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        try {
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Permesso notifiche non concesso", e)
        }
    }

    private fun getDayName(day: DayOfWeek): Int {
        return when (day) {
            DayOfWeek.MONDAY -> R.string.monday
            DayOfWeek.TUESDAY -> R.string.tuesday
            DayOfWeek.WEDNESDAY -> R.string.wednesday
            DayOfWeek.THURSDAY -> R.string.thursday
            DayOfWeek.FRIDAY -> R.string.friday
            DayOfWeek.SATURDAY -> R.string.saturday
            DayOfWeek.SUNDAY -> R.string.sunday
        }
    }
}
