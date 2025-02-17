package com.tempo.tempoapp.data

import android.content.Context
import com.tempo.tempoapp.data.repository.AccelerometerRepository
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.BloodGlucoseRepository
import com.tempo.tempoapp.data.repository.BloodPressureRepository
import com.tempo.tempoapp.data.repository.BodyFatRepository
import com.tempo.tempoapp.data.repository.BodyWaterMassRepository
import com.tempo.tempoapp.data.repository.BoneMassRepository
import com.tempo.tempoapp.data.repository.DistanceRepository
import com.tempo.tempoapp.data.repository.ElevationGainedRepository
import com.tempo.tempoapp.data.repository.FloorsClimbedRepository
import com.tempo.tempoapp.data.repository.HeartRateRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.data.repository.OxygenSaturationRepository
import com.tempo.tempoapp.data.repository.ReminderRepository
import com.tempo.tempoapp.data.repository.RespiratoryRateRepository
import com.tempo.tempoapp.data.repository.SleepSessionRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.data.repository.TotalCaloriesBurnedRepository
import com.tempo.tempoapp.data.repository.UtilsRepository
import com.tempo.tempoapp.data.repository.WeatherForecastRepository
import com.tempo.tempoapp.data.repository.WeightRepository

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
    val totalCaloriesBurnedRepository: TotalCaloriesBurnedRepository
    val bloodGlucoseRepository: BloodGlucoseRepository
    val bloodPressureRepository: BloodPressureRepository
    val bodyFatRepository: BodyFatRepository
    val bodyWaterMassRepository: BodyWaterMassRepository
    val boneMassRepository: BoneMassRepository
    val distanceRepository: DistanceRepository
    val elevationGainedRepository: ElevationGainedRepository
    val floorsClimbedRepository: FloorsClimbedRepository
    val oxygenSaturationRepository: OxygenSaturationRepository
    val respiratoryRateRepository: RespiratoryRateRepository
    val sleepSessionRepository: SleepSessionRepository
    val weightRepository: WeightRepository
    val heartRateRepository: HeartRateRepository

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
    override val totalCaloriesBurnedRepository: TotalCaloriesBurnedRepository
        get() = TotalCaloriesBurnedRepository(
            TempoDatabase.getDatabase(context).totalCaloriesBurnedDao()
        )
    override val bloodGlucoseRepository: BloodGlucoseRepository
        get() = BloodGlucoseRepository(TempoDatabase.getDatabase(context).bloodGlucoseDao())
    override val bloodPressureRepository: BloodPressureRepository
        get() = BloodPressureRepository(TempoDatabase.getDatabase(context).bloodPressureDao())
    override val bodyFatRepository: BodyFatRepository
        get() = BodyFatRepository(TempoDatabase.getDatabase(context).bodyFatDao())
    override val bodyWaterMassRepository: BodyWaterMassRepository
        get() = BodyWaterMassRepository(TempoDatabase.getDatabase(context).bodyWaterMassDao())
    override val boneMassRepository: BoneMassRepository
        get() = BoneMassRepository(TempoDatabase.getDatabase(context).boneMassDao())
    override val distanceRepository: DistanceRepository
        get() = DistanceRepository(TempoDatabase.getDatabase(context).distanceDao())
    override val elevationGainedRepository: ElevationGainedRepository
        get() = ElevationGainedRepository(TempoDatabase.getDatabase(context).elevationGainedDao())
    override val floorsClimbedRepository: FloorsClimbedRepository
        get() = FloorsClimbedRepository(TempoDatabase.getDatabase(context).floorsClimbedDao())
    override val oxygenSaturationRepository: OxygenSaturationRepository
        get() = OxygenSaturationRepository(TempoDatabase.getDatabase(context).oxygenSaturationDao())
    override val respiratoryRateRepository: RespiratoryRateRepository
        get() = RespiratoryRateRepository(TempoDatabase.getDatabase(context).respiratoryRateDao())
    override val sleepSessionRepository: SleepSessionRepository
        get() = SleepSessionRepository(TempoDatabase.getDatabase(context).sleepSessionDao())
    override val weightRepository: WeightRepository
        get() = WeightRepository(TempoDatabase.getDatabase(context).weightDao())
    override val heartRateRepository: HeartRateRepository
        get() = HeartRateRepository(TempoDatabase.getDatabase(context).heartRateDao())
}