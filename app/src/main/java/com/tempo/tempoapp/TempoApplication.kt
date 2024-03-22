package com.tempo.tempoapp

import android.app.Application
import androidx.work.WorkManager
import com.tempo.tempoapp.data.AppContainer
import com.tempo.tempoapp.data.AppDataContainer
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager

class TempoApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
    lateinit var healthConnectManager: HealthConnectManager
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        healthConnectManager = HealthConnectManager(this)
        workManager = WorkManager.getInstance(this)
    }
}