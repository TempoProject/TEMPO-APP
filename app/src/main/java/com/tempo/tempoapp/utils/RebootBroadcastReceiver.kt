package com.tempo.tempoapp.utils

import AppPreferencesManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tempo.tempoapp.ui.prophylaxis.SchedulingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {

            Log.d(TAG, "Dispositivo riavviato, ripristino allarmi")
            CoroutineScope(Dispatchers.Default).launch {
                restoreAlarms(context)
            }
        }
    }

    private suspend fun restoreAlarms(context: Context) {
        try {
            val appPreferencesManager = AppPreferencesManager(context)
            val config = appPreferencesManager.prophylaxisConfig.firstOrNull()
            val isActive = appPreferencesManager.isActiveProphylaxis.first()

            if (config != null && isActive) {
                Log.d(TAG, "Ripristino configurazione: $config")

                val scheduler = ProphylaxisScheduler(context)

                when (config.schedulingMode) {
                    SchedulingMode.DaysOfWeek -> {
                        config.selectedDays?.let { day ->
                            scheduler.scheduleWeeklyAlarms(
                                selectedDays = setOf(day), // Converti in Set
                                hour = config.hour,
                                minute = config.minute
                            )
                        }
                    }

                    SchedulingMode.Recurring -> {
                        scheduler.scheduleRecurringAlarm(
                            interval = config.recurrenceInterval,
                            unit = config.recurrenceUnit,
                            startDate = config.startDate ?: LocalDate.now(),
                            hour = config.hour,
                            minute = config.minute
                        )
                    }
                }

                Log.d(TAG, "Allarmi ripristinati con successo")
            } else {
                Log.d(TAG, "Nessuna configurazione attiva da ripristinare")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel ripristino degli allarmi", e)
        }
    }
}

