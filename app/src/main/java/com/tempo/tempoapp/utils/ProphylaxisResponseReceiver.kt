package com.tempo.tempoapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ProphylaxisResponseReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_YES_RESPONSE = "com.tempo.ACTION_YES_RESPONSE"
        const val ACTION_NO_RESPONSE = "com.tempo.ACTION_NO_RESPONSE"
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
}