package com.tempo.tempoapp.data

import android.content.Context
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository

interface AppContainer {
    val bleedingRepository: BleedingRepository
    val infusionRepository: InfusionRepository
    val stepsRecordRepository: StepsRecordRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val bleedingRepository: BleedingRepository by lazy {
        BleedingRepository(TempoDatabase.getDatabase(context).bleedingDao())
    }
    override val infusionRepository: InfusionRepository by lazy {
        InfusionRepository(TempoDatabase.getDatabase(context).infusionDao())
    }
    override val stepsRecordRepository: StepsRecordRepository by lazy {
        StepsRecordRepository(TempoDatabase.getDatabase(context).stepsDao())
    }

}