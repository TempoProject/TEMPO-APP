package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.BloodGlucoseDao
import com.tempo.tempoapp.data.dao.BloodPressureDao
import com.tempo.tempoapp.data.dao.BodyFatDao
import com.tempo.tempoapp.data.dao.BodyWaterMassDao
import com.tempo.tempoapp.data.dao.BoneMassDao
import com.tempo.tempoapp.data.dao.DistanceDao
import com.tempo.tempoapp.data.dao.ElevationGainedDao
import com.tempo.tempoapp.data.dao.FloorsClimbedDao
import com.tempo.tempoapp.data.dao.HeartRateDao
import com.tempo.tempoapp.data.dao.OxygenSaturationDao
import com.tempo.tempoapp.data.dao.RespiratoryRateDao
import com.tempo.tempoapp.data.dao.SleepSessionDao
import com.tempo.tempoapp.data.dao.TotalCaloriesBurnedDao
import com.tempo.tempoapp.data.dao.WeightDao
import com.tempo.tempoapp.data.model.BloodGlucose
import com.tempo.tempoapp.data.model.BloodPressure
import com.tempo.tempoapp.data.model.BodyFat
import com.tempo.tempoapp.data.model.BodyWaterMass
import com.tempo.tempoapp.data.model.BoneMass
import com.tempo.tempoapp.data.model.Distance
import com.tempo.tempoapp.data.model.ElevationGained
import com.tempo.tempoapp.data.model.FloorsClimbed
import com.tempo.tempoapp.data.model.HeartRate
import com.tempo.tempoapp.data.model.OxygenSaturation
import com.tempo.tempoapp.data.model.RespiratoryRate
import com.tempo.tempoapp.data.model.SleepSession
import com.tempo.tempoapp.data.model.TotalCaloriesBurned
import com.tempo.tempoapp.data.model.Weight
import kotlinx.coroutines.flow.Flow

class TotalCaloriesBurnedRepository(private val totalCaloriesBurnedDao: TotalCaloriesBurnedDao) :
    LogbookRepository<TotalCaloriesBurned> {
    override suspend fun insertItem(item: TotalCaloriesBurned): Long {
        return totalCaloriesBurnedDao.insert(item)
    }

    override suspend fun deleteItem(item: TotalCaloriesBurned) {
        totalCaloriesBurnedDao.delete(item)
    }

    override suspend fun updateItem(item: TotalCaloriesBurned) {
        totalCaloriesBurnedDao.update(item)
    }

    override fun getAll(): Flow<List<TotalCaloriesBurned>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<TotalCaloriesBurned> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayTotalCaloriesBurned(isSent: Boolean): List<TotalCaloriesBurned> {
        return totalCaloriesBurnedDao.getAllDayTotalCaloriesBurned(isSent)
    }
}

class BloodGlucoseRepository(private val bloodGlucoseDao: BloodGlucoseDao) :
    LogbookRepository<BloodGlucose> {
    override suspend fun insertItem(item: BloodGlucose): Long {
        return bloodGlucoseDao.insert(item)
    }

    override suspend fun deleteItem(item: BloodGlucose) {
        bloodGlucoseDao.delete(item)
    }

    override suspend fun updateItem(item: BloodGlucose) {
        bloodGlucoseDao.update(item)
    }

    override fun getAll(): Flow<List<BloodGlucose>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<BloodGlucose> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayBloodGlucose(isSent: Boolean): List<BloodGlucose> {
        return bloodGlucoseDao.getAllDayBloodGlucose(isSent)
    }
}

class BloodPressureRepository(private val bloodPressureDao: BloodPressureDao) :
    LogbookRepository<BloodPressure> {
    override suspend fun insertItem(item: BloodPressure): Long {
        return bloodPressureDao.insert(item)
    }

    override suspend fun deleteItem(item: BloodPressure) {
        bloodPressureDao.delete(item)
    }

    override suspend fun updateItem(item: BloodPressure) {
        bloodPressureDao.update(item)
    }

    override fun getAll(): Flow<List<BloodPressure>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<BloodPressure> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayBloodPressure(isSent: Boolean): List<BloodPressure> {
        return bloodPressureDao.getAllDayBloodPressure(isSent)
    }
}

class BodyFatRepository(private val bodyFatDao: BodyFatDao) : LogbookRepository<BodyFat> {
    override suspend fun insertItem(item: BodyFat): Long {
        return bodyFatDao.insert(item)
    }

    override suspend fun deleteItem(item: BodyFat) {
        bodyFatDao.delete(item)
    }

    override suspend fun updateItem(item: BodyFat) {
        bodyFatDao.update(item)
    }

    override fun getAll(): Flow<List<BodyFat>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<BodyFat> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayBodyFat(isSent: Boolean): List<BodyFat> {
        return bodyFatDao.getAllDayBodyFat(isSent)
    }
}

class BodyWaterMassRepository(private val bodyWaterMassDao: BodyWaterMassDao) :
    LogbookRepository<BodyWaterMass> {
    override suspend fun insertItem(item: BodyWaterMass): Long {
        return bodyWaterMassDao.insert(item)
    }

    override suspend fun deleteItem(item: BodyWaterMass) {
        bodyWaterMassDao.delete(item)
    }

    override suspend fun updateItem(item: BodyWaterMass) {
        bodyWaterMassDao.update(item)
    }

    override fun getAll(): Flow<List<BodyWaterMass>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<BodyWaterMass> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayBodyWaterMass(isSent: Boolean): List<BodyWaterMass> {
        return bodyWaterMassDao.getAllDayBodyWaterMass(isSent)
    }
}

class BoneMassRepository(private val boneMassDao: BoneMassDao) : LogbookRepository<BoneMass> {
    override suspend fun insertItem(item: BoneMass): Long {
        return boneMassDao.insert(item)
    }

    override suspend fun deleteItem(item: BoneMass) {
        boneMassDao.delete(item)
    }

    override suspend fun updateItem(item: BoneMass) {
        boneMassDao.update(item)
    }

    override fun getAll(): Flow<List<BoneMass>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<BoneMass> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayBoneMass(isSent: Boolean): List<BoneMass> {
        return boneMassDao.getAllDayBoneMass(isSent)
    }

}

class DistanceRepository(private val distanceDao: DistanceDao) : LogbookRepository<Distance> {
    override suspend fun insertItem(item: Distance): Long {
        return distanceDao.insert(item)
    }

    override suspend fun deleteItem(item: Distance) {
        distanceDao.delete(item)
    }

    override suspend fun updateItem(item: Distance) {
        distanceDao.update(item)
    }

    override fun getAll(): Flow<List<Distance>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Distance> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayDistance(isSent: Boolean): List<Distance> {
        return distanceDao.getAllDayDistance(isSent)
    }
}

class ElevationGainedRepository(private val elevationGainedDao: ElevationGainedDao) :
    LogbookRepository<ElevationGained> {
    override suspend fun insertItem(item: ElevationGained): Long {
        return elevationGainedDao.insert(item)
    }

    override suspend fun deleteItem(item: ElevationGained) {
        elevationGainedDao.delete(item)
    }

    override suspend fun updateItem(item: ElevationGained) {
        elevationGainedDao.update(item)
    }

    override fun getAll(): Flow<List<ElevationGained>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<ElevationGained> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayElevationGained(isSent: Boolean): List<ElevationGained> {
        return elevationGainedDao.getAllDayElevationGained(isSent)
    }
}

class FloorsClimbedRepository(private val floorsClimbedDao: FloorsClimbedDao) :
    LogbookRepository<FloorsClimbed> {
    override suspend fun insertItem(item: FloorsClimbed): Long {
        return floorsClimbedDao.insert(item)
    }

    override suspend fun deleteItem(item: FloorsClimbed) {
        floorsClimbedDao.delete(item)
    }

    override suspend fun updateItem(item: FloorsClimbed) {
        floorsClimbedDao.update(item)
    }

    override fun getAll(): Flow<List<FloorsClimbed>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<FloorsClimbed> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayFloorsClimbed(isSent: Boolean): List<FloorsClimbed> {
        return floorsClimbedDao.getAllDayFloorsClimbed(isSent)
    }
}

class OxygenSaturationRepository(private val oxygenSaturationDao: OxygenSaturationDao) :
    LogbookRepository<OxygenSaturation> {
    override suspend fun insertItem(item: OxygenSaturation): Long {
        return oxygenSaturationDao.insert(item)
    }

    override suspend fun deleteItem(item: OxygenSaturation) {
        oxygenSaturationDao.delete(item)
    }

    override suspend fun updateItem(item: OxygenSaturation) {
        oxygenSaturationDao.update(item)
    }

    override fun getAll(): Flow<List<OxygenSaturation>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<OxygenSaturation> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayOxygenSaturation(isSent: Boolean): List<OxygenSaturation> {
        return oxygenSaturationDao.getAllDayOxygenSaturation(isSent)
    }
}

class RespiratoryRateRepository(private val respiratoryRateDao: RespiratoryRateDao) :
    LogbookRepository<RespiratoryRate> {
    override suspend fun insertItem(item: RespiratoryRate): Long {
        return respiratoryRateDao.insert(item)
    }

    override suspend fun deleteItem(item: RespiratoryRate) {
        respiratoryRateDao.delete(item)
    }

    override suspend fun updateItem(item: RespiratoryRate) {
        respiratoryRateDao.update(item)
    }

    override fun getAll(): Flow<List<RespiratoryRate>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<RespiratoryRate> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayRespiratoryRate(isSent: Boolean): List<RespiratoryRate> {
        return respiratoryRateDao.getAllDayRespiratoryRate(isSent)
    }
}

class SleepSessionRepository(private val sleepSessionDao: SleepSessionDao) :
    LogbookRepository<SleepSession> {
    override suspend fun insertItem(item: SleepSession): Long {
        return sleepSessionDao.insert(item)
    }

    override suspend fun deleteItem(item: SleepSession) {
        sleepSessionDao.delete(item)
    }

    override suspend fun updateItem(item: SleepSession) {
        sleepSessionDao.update(item)
    }

    override fun getAll(): Flow<List<SleepSession>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<SleepSession> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDaySleepSessions(isSent: Boolean): List<SleepSession> {
        return sleepSessionDao.getAllDaySleepSession(isSent)
    }
}

class WeightRepository(private val weightDao: WeightDao) : LogbookRepository<Weight> {
    override suspend fun insertItem(item: Weight): Long {
        return weightDao.insert(item)
    }

    override suspend fun deleteItem(item: Weight) {
        weightDao.delete(item)
    }

    override suspend fun updateItem(item: Weight) {
        weightDao.update(item)
    }

    override fun getAll(): Flow<List<Weight>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<Weight> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayWeight(isSent: Boolean): List<Weight> {
        return weightDao.getAllDayWeight(isSent)
    }
}

class HeartRateRepository(private val heartRateDao: HeartRateDao) : LogbookRepository<HeartRate> {
    override suspend fun insertItem(item: HeartRate): Long {
        return heartRateDao.insert(item)
    }

    override suspend fun deleteItem(item: HeartRate) {
        heartRateDao.delete(item)
    }

    override suspend fun updateItem(item: HeartRate) {
        heartRateDao.update(item)
    }

    override fun getAll(): Flow<List<HeartRate>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<HeartRate> {
        TODO("Not yet implemented")
    }

    suspend fun getAllDayHeartRate(isSent: Boolean): List<HeartRate> {
        return heartRateDao.getAllDayHeartRate(isSent)
    }
}