package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.BleedingEventDao
import com.tempo.tempoapp.data.dao.InfusionEventDao
import com.tempo.tempoapp.data.dao.ProphylaxisResponseDao
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.ProphylaxisResponse

class ExportRepository(
    private val bleedingDao: BleedingEventDao,
    private val infusionDao: InfusionEventDao,
    private val prophylaxisDao: ProphylaxisResponseDao
) {
    suspend fun getAllBleedingEvents(): List<BleedingEvent> = bleedingDao.getAllBleedingEvents()
    suspend fun getAllInfusionEvents(): List<InfusionEvent> = infusionDao.getAllInfusionEvents()
    suspend fun getAllProphylaxisResponses(): List<ProphylaxisResponse> = prophylaxisDao.getAllProphylaxisResponses()
}