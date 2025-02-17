package com.tempo.tempoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tempo.tempoapp.data.dao.AccelerometerDao
import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.dao.BloodGlucoseDao
import com.tempo.tempoapp.data.dao.BloodPressureDao
import com.tempo.tempoapp.data.dao.BodyFatDao
import com.tempo.tempoapp.data.dao.BodyWaterMassDao
import com.tempo.tempoapp.data.dao.BoneMassDao
import com.tempo.tempoapp.data.dao.DistanceDao
import com.tempo.tempoapp.data.dao.ElevationGainedDao
import com.tempo.tempoapp.data.dao.FloorsClimbedDao
import com.tempo.tempoapp.data.dao.HeartRateDao
import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.dao.MovesenseDao
import com.tempo.tempoapp.data.dao.OxygenSaturationDao
import com.tempo.tempoapp.data.dao.ReminderDao
import com.tempo.tempoapp.data.dao.RespiratoryRateDao
import com.tempo.tempoapp.data.dao.SleepSessionDao
import com.tempo.tempoapp.data.dao.StepsRecordDao
import com.tempo.tempoapp.data.dao.TotalCaloriesBurnedDao
import com.tempo.tempoapp.data.dao.UtilsDao
import com.tempo.tempoapp.data.dao.WeatherForecastDao
import com.tempo.tempoapp.data.dao.WeightDao
import com.tempo.tempoapp.data.model.Accelerometer
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.BloodGlucose
import com.tempo.tempoapp.data.model.BloodPressure
import com.tempo.tempoapp.data.model.BodyFat
import com.tempo.tempoapp.data.model.BodyWaterMass
import com.tempo.tempoapp.data.model.BoneMass
import com.tempo.tempoapp.data.model.Distance
import com.tempo.tempoapp.data.model.ElevationGained
import com.tempo.tempoapp.data.model.FloorsClimbed
import com.tempo.tempoapp.data.model.HeartRate
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.model.OxygenSaturation
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.model.RespiratoryRate
import com.tempo.tempoapp.data.model.SleepSession
import com.tempo.tempoapp.data.model.StepsRecord
import com.tempo.tempoapp.data.model.TotalCaloriesBurned
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.WeatherForecast
import com.tempo.tempoapp.data.model.Weight

/**
 * Database class representing the SQLite database for the application.
 * Defines entities and provides DAOs for accessing data tables.
 *
 * @param entities Array of entity classes representing database tables.
 * @param version The version number of the database schema.
 * @param exportSchema Whether to export the database schema.
 */
@Database(
    entities = arrayOf(
        BleedingEvent::class,
        InfusionEvent::class,
        StepsRecord::class,
        Utils::class,
        Movesense::class,
        Accelerometer::class,
        ReminderEvent::class,
        WeatherForecast::class,
        TotalCaloriesBurned::class,
        BloodGlucose::class,
        BloodPressure::class,
        HeartRate::class,
        BodyFat::class,
        BodyWaterMass::class,
        BoneMass::class,
        Distance::class,
        ElevationGained::class,
        FloorsClimbed::class,
        OxygenSaturation::class,
        RespiratoryRate::class,
        SleepSession::class,
        Weight::class

    ),
    version = 1,
    exportSchema = true
)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun bleedingDao(): BleedingEventDao
    abstract fun infusionDao(): InfusionEventDao
    abstract fun stepsDao(): StepsRecordDao
    abstract fun utilsDao(): UtilsDao
    abstract fun movesenseDao(): MovesenseDao
    abstract fun accelerometerDao(): AccelerometerDao
    abstract fun reminderDao(): ReminderDao
    abstract fun weatherForecastDao(): WeatherForecastDao
    abstract fun totalCaloriesBurnedDao(): TotalCaloriesBurnedDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun bodyFatDao(): BodyFatDao
    abstract fun bodyWaterMassDao(): BodyWaterMassDao
    abstract fun boneMassDao(): BoneMassDao
    abstract fun distanceDao(): DistanceDao
    abstract fun elevationGainedDao(): ElevationGainedDao
    abstract fun floorsClimbedDao(): FloorsClimbedDao
    abstract fun oxygenSaturationDao(): OxygenSaturationDao
    abstract fun respiratoryRateDao(): RespiratoryRateDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun weightDao(): WeightDao
    abstract fun heartRateDao(): HeartRateDao

    companion object {
        @Volatile
        private var INSTANCE: TempoDatabase? = null

        /**
         * Retrieves an instance of the TempoDatabase.
         * If an instance does not exist, creates a new one.
         *
         * @param context The application context.
         * @return An instance of TempoDatabase.
         */
        fun getDatabase(context: Context): TempoDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TempoDatabase::class.java,
                    "logbook_database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}