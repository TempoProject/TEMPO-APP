package com.tempo.tempoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.dao.StepsRecordDao
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.StepsRecord

@Database(
    entities = arrayOf(BleedingEvent::class, InfusionEvent::class, StepsRecord::class),
    version = 1,
    exportSchema = false
)
abstract class TempoDatabase : RoomDatabase() {
    abstract fun bleedingDao(): BleedingEventDao
    abstract fun infusionDao(): InfusionEventDao
    abstract fun stepsDao(): StepsRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TempoDatabase? = null

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