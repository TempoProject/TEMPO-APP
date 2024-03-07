package com.tempo.tempoapp.data

import android.content.Context
import com.tempo.tempoapp.data.repository.BleedingRepository

interface AppContainer {
    val bleedingRepository: BleedingRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val bleedingRepository: BleedingRepository by lazy {
        BleedingRepository(TempoDatabase.getDatabase(context).bleedingDao())
    }
}