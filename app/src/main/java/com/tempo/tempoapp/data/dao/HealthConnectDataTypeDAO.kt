package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
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

@Dao
interface TotalCaloriesBurnedDao : LogbookDao<TotalCaloriesBurned> {
    @Query(
        """
        SELECT *
        FROM total_calories_burned
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayTotalCaloriesBurned(isSent: Boolean): List<TotalCaloriesBurned>
}

@Dao
interface BloodGlucoseDao : LogbookDao<BloodGlucose> {
    @Query(
        """
        SELECT *
        FROM blood_glucose
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayBloodGlucose(isSent: Boolean): List<BloodGlucose>
}

@Dao
interface BloodPressureDao : LogbookDao<BloodPressure> {
    @Query(
        """
        SELECT *
        FROM blood_pressure
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayBloodPressure(isSent: Boolean): List<BloodPressure>
}

@Dao
interface BodyFatDao : LogbookDao<BodyFat> {
    @Query(
        """
        SELECT *
        FROM body_fat
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayBodyFat(isSent: Boolean): List<BodyFat>
}

@Dao
interface BodyWaterMassDao : LogbookDao<BodyWaterMass> {
    @Query(
        """
        SELECT *
        FROM body_water_mass
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayBodyWaterMass(isSent: Boolean): List<BodyWaterMass>
}

@Dao
interface BoneMassDao : LogbookDao<BoneMass> {
    @Query(
        """
        SELECT *
        FROM bone_mass
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayBoneMass(isSent: Boolean): List<BoneMass>
}

@Dao
interface DistanceDao : LogbookDao<Distance> {
    @Query(
        """
        SELECT *
        FROM distance
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayDistance(isSent: Boolean): List<Distance>
}

@Dao
interface ElevationGainedDao : LogbookDao<ElevationGained> {
    @Query(
        """
        SELECT *
        FROM elevation_gained
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayElevationGained(isSent: Boolean): List<ElevationGained>
}

@Dao
interface FloorsClimbedDao : LogbookDao<FloorsClimbed> {
    @Query(
        """
        SELECT *
        FROM floors_climbed
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayFloorsClimbed(isSent: Boolean): List<FloorsClimbed>
}

@Dao
interface OxygenSaturationDao : LogbookDao<OxygenSaturation> {
    @Query(
        """
        SELECT *
        FROM oxygen_saturation
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayOxygenSaturation(isSent: Boolean): List<OxygenSaturation>
}

@Dao
interface RespiratoryRateDao : LogbookDao<RespiratoryRate> {
    @Query(
        """
        SELECT *
        FROM respiratory_rate
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayRespiratoryRate(isSent: Boolean): List<RespiratoryRate>
}

@Dao
interface SleepSessionDao : LogbookDao<SleepSession> {
    @Query(
        """
        SELECT *
        FROM sleep_session
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDaySleepSession(isSent: Boolean): List<SleepSession>
}

@Dao
interface WeightDao : LogbookDao<Weight> {
    @Query(
        """
        SELECT *
        FROM weight
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayWeight(isSent: Boolean): List<Weight>
}

@Dao
interface HeartRateDao : LogbookDao<HeartRate> {
    @Query(
        """
        SELECT *
        FROM heart_rate
        WHERE is_sent = :isSent
        """
    )
    suspend fun getAllDayHeartRate(isSent: Boolean): List<HeartRate>
}