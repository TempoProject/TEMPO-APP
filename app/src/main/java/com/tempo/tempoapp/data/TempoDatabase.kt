package com.tempo.tempoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tempo.tempoapp.data.dao.AccelerometerDao
import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.dao.MovesenseDao
import com.tempo.tempoapp.data.dao.ReminderDao
import com.tempo.tempoapp.data.dao.StepsRecordDao
import com.tempo.tempoapp.data.dao.UtilsDao
import com.tempo.tempoapp.data.dao.WeatherForecastDao
import com.tempo.tempoapp.data.model.Accelerometer
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.data.model.StepsRecord
import com.tempo.tempoapp.data.model.Utils
import com.tempo.tempoapp.data.model.WeatherForecast

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
        WeatherForecast::class
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