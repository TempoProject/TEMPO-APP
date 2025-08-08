package com.tempo.tempoapp.workers

import AppPreferencesManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.BuildConfig
import com.tempo.tempoapp.utils.ApiLogin
import com.tempo.tempoapp.utils.UserLoginRequest
import kotlinx.coroutines.flow.first


class SessionID(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val appPreferencesManager = AppPreferencesManager(ctx)

    override suspend fun doWork(): Result {

        // Renew session id
        val response = ApiLogin.retrofitService.sessionID(
            UserLoginRequest(
                email = BuildConfig.APP_EMAIL,
                password = BuildConfig.APP_PASSWORD
            )
        )

        if (!response.isSuccessful) {
            Log.d("SessionID", "Failed to renew session ID: ${response.errorBody()?.string()}")
            return Result.failure()
        }

        val sessionId = appPreferencesManager.sessionId.first()

        if (!response.body()?.session?.sid.isNullOrEmpty() && sessionId != response.body()?.session?.sid) {
            appPreferencesManager.setSessionId(
                response.body()?.session?.sid ?: ""
            )

            Log.d("SessionID", "Session ID updated: ${response.body()?.session?.sid}")
        }
        return Result.success()
    }

}