package com.tempo.tempoapp

import android.app.Application
import com.tempo.tempoapp.data.AppContainer
import com.tempo.tempoapp.data.AppDataContainer

class TempoApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}