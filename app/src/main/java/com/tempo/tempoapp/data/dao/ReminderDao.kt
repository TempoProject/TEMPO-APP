package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import com.tempo.tempoapp.data.model.ReminderEvent

@Dao
interface ReminderDao : LogbookDao<ReminderEvent>