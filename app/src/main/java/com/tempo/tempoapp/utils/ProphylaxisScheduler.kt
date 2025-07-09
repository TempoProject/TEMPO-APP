package com.tempo.tempoapp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.prophylaxis.RecurrenceUnit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class ProphylaxisScheduler(private val context: Context) {
    companion object {
        private const val WEEKLY_ALARM_BASE_ID = 1000
        private const val RECURRING_ALARM_ID = 2000
        private const val TAG = "ProphylaxisScheduler"
    }

    fun scheduleWeeklyAlarms(
        selectedDays: Set<DayOfWeek>, // Set per compatibilità, ma conterrà solo 1 elemento
        hour: Int,
        minute: Int
    ) {
        Log.d(TAG, "Scheduling settimanale per giorni: $selectedDays alle $hour:$minute")

        selectedDays.firstOrNull()?.let { day ->
            val now = LocalDateTime.now()
            val todayWithTime = now.withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)

            val nextTrigger = if (now.dayOfWeek == day && now.isAfter(todayWithTime)) {
                now.with(TemporalAdjusters.next(day))
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0)
            } else {
                now.with(TemporalAdjusters.nextOrSame(day))
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0)
            }

            val millis = nextTrigger.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()

            Log.d(TAG, "Prossimo trigger per $day: $nextTrigger ($millis)")

            scheduleAlarm(
                alarmId = WEEKLY_ALARM_BASE_ID, // ID fisso per singolo giorno
                triggerAtMillis = millis,
                title = context.getString(R.string.weekly_prophylaxis),
                message = "${context.getString(R.string.prophylaxis_time_notification)} ${
                    context.getString(getDayName(day))
                }"
            )
        }
    }

    fun scheduleRecurringAlarm(
        interval: Int,
        unit: RecurrenceUnit,
        startDate: LocalDate,
        hour: Int,
        minute: Int
    ) {
        Log.d(TAG, "Scheduling ricorrente ogni $interval $unit dalle $startDate alle $hour:$minute")

        val now = LocalDateTime.now()
        val startDateTime = startDate.atTime(hour, minute)

        val nextTrigger = if (now.isBefore(startDateTime)) {
            startDateTime
        } else {
            val daysToAdd = when (unit) {
                RecurrenceUnit.Days -> interval.toLong()
                RecurrenceUnit.Weeks -> (interval * 7).toLong()
            }
            val daysSinceStart = ChronoUnit.DAYS.between(startDate, now.toLocalDate())
            val intervalsPassed = (daysSinceStart / daysToAdd) + 1

            startDate.plusDays(intervalsPassed * daysToAdd)
                .atTime(hour, minute)
        }

        val millis = nextTrigger.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        Log.d(TAG, "Prossimo trigger ricorrente: $nextTrigger ($millis)")

        scheduleAlarm(
            alarmId = RECURRING_ALARM_ID,
            triggerAtMillis = millis,
            title = context.getString(R.string.recurring_prophylaxis),
            message = context.getString(R.string.prophylaxis_time)
        )
    }


    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(
        alarmId: Int,
        triggerAtMillis: Long,
        title: String,
        message: String
    ) {
        Log.d("Scheduler", "Pianifico alarm ID $alarmId: $title - $message alle $triggerAtMillis")

        val intent = Intent(context, ProphylaxisAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("alarmId", alarmId) // Aggiungi l'ID per il re-scheduling
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(
                "Scheduler",
                "Alarm $alarmId programmato con successo per ${java.util.Date(triggerAtMillis)}"
            )
        } catch (e: Exception) {
            Log.e("Scheduler", "Errore nella programmazione dell'alarm $alarmId", e)
        }
    }

    fun cancelAllAlarms() {
        Log.d("Scheduler", "Cancellazione di tutti gli allarmi")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancella allarme settimanale (solo 1 con singolo giorno)
        cancelAlarm(alarmManager, WEEKLY_ALARM_BASE_ID)

        // Cancella allarme ricorrente
        cancelAlarm(alarmManager, RECURRING_ALARM_ID)
    }

    private fun cancelAlarm(alarmManager: AlarmManager, alarmId: Int) {
        try {
            val intent = Intent(context, ProphylaxisAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("Scheduler", "Alarm $alarmId cancellato")
        } catch (e: Exception) {
            Log.e("Scheduler", "Errore nella cancellazione dell'alarm $alarmId", e)
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



