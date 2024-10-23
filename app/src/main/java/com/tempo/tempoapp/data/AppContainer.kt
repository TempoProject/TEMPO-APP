package com.tempo.tempoapp.data

import android.content.Context import com.tempo.tempoapp.data.repository.AccelerometerRepository
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.data.repository.UtilsRepository
import com.tempo.tempoapp.data.repository.WeatherForecastRepository

/**
 * Interface defining a contract for managing repositories within the Android application.
 * Provides access to various repositories required for data management.
 */
interface AppContainer {
    val bleedingRepository: BleedingRepository
    val infusionRepository: InfusionRepository
    val stepsRecordRepository: StepsRecordRepository
    val utilsRepository: UtilsRepository
    val movesenseRepository: MovesenseRepository
    val accelerometerRepository: AccelerometerRepository
    val reminderRepository: ReminderRepository
    val weatherForecastRepository: WeatherForecastRepository
}

/**
 * Class implementing the AppContainer interface, providing concrete implementations for each repository.
 * Repositories are lazily initialized using database access obtained from the provided Context.
 *
 * @param context The application context used for accessing the database.
 */
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
    override val utilsRepository: UtilsRepository by lazy {
        UtilsRepository(TempoDatabase.getDatabase(context).utilsDao())
    }
    override val movesenseRepository: MovesenseRepository by lazy {
        MovesenseRepository(TempoDatabase.getDatabase(context).movesenseDao())
    }
    override val accelerometerRepository: AccelerometerRepository by lazy {
        AccelerometerRepository(TempoDatabase.getDatabase(context).accelerometerDao())
    }
    override val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(TempoDatabase.getDatabase(context).reminderDao())
    }
    override val weatherForecastRepository: WeatherForecastRepository by lazy {
        WeatherForecastRepository(TempoDatabase.getDatabase(context).weatherForecastDao())
    }
}