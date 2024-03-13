package com.tempo.tempoapp.data

import android.content.Context
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository

interface AppContainer {
    val bleedingRepository: BleedingRepository
    val infusionRepository: InfusionRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val bleedingRepository: BleedingRepository by lazy {
        BleedingRepository(TempoDatabase.getDatabase(context).bleedingDao())
    }
    override val infusionRepository: InfusionRepository by lazy {
        InfusionRepository(TempoDatabase.getDatabase(context).infusionDao())
    }

}