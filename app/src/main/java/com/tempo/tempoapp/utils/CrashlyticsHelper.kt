package com.tempo.tempoapp.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsHelper {

    fun logCriticalAction(action: String, success: Boolean, details: String? = null) {
        try {
            with(FirebaseCrashlytics.getInstance()) {
                log("Critical action: $action - Success: $success")
                setCustomKey("last_critical_action", action)
                setCustomKey("last_action_success", success)
                details?.let {
                    log("Details: $it")
                }
            }
            Log.d("CrashlyticsHelper", "Logged critical action: $action")
        } catch (e: Exception) {
            Log.e("CrashlyticsHelper", "Failed to log critical action", e)
        }
    }


    fun logPerformanceMetric(operation: String, durationMs: Long, success: Boolean) {
        try {
            with(FirebaseCrashlytics.getInstance()) {
                log("Performance: $operation took ${durationMs}ms - Success: $success")
                setCustomKey("last_operation", operation)
                setCustomKey("last_operation_duration", durationMs)
                setCustomKey("last_operation_success", success)
            }
        } catch (e: Exception) {
            Log.e("CrashlyticsHelper", "Failed to log performance metric", e)
        }
    }
}