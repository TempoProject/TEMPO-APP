package com.tempo.tempoapp.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * StepsReceiver is a BroadcastReceiver responsible for handling steps-related broadcasts.

 */
@SuppressLint("RestrictedApi")
class StepsReceiver :
    BroadcastReceiver() {

    companion object {
        private val TAG = StepsReceiver::class.java.simpleName
    }

    /**
     * Receives the broadcast and starts the StepsService as a foreground service.
     *
     * @param context The context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d(TAG, "Received broadcast")
        val instant = intent?.getLongExtra("instant", Instant.now().toEpochMilli())
        val newInstant = Instant.ofEpochMilli(instant!!).plus(30, ChronoUnit.MINUTES).toEpochMilli()
        val newIntent = Intent(context, StepsReceiver::class.java)
        newIntent.putExtra(
            "instant",
            newInstant
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            newInstant.toInt(),
            newIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "Scheduling next alarm with instant $newInstant")
        AlarmManagerHelper(context!!).scheduleStepsService(pendingIntent, newInstant)
        Log.d(TAG, "Starting steps service")
        val serviceIntent = Intent(context, StepsService::class.java)
        context.startForegroundService(serviceIntent)
    }
}

/**
 * Retrieves a Serializable object from the Intent.
 *
 * @param key The key with which the Serializable object was added to the Intent.
 * @param mClass The class of the Serializable object.
 * @return The Serializable object retrieved from the Intent.
 */
@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T : Serializable?> Intent.getSerializable(key: String, mClass: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        this.getSerializableExtra(key, mClass)!!
    else
        this.getSerializableExtra(key) as T
}