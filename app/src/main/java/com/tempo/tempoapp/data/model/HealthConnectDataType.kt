package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "total_calories_burned", indices = [Index(value = ["record_id"], unique = true)])
data class TotalCaloriesBurned(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "calories")
    val calories: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class TotalCaloriesBurnedToJson(
    val id: Int = 0,
    val calories: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun TotalCaloriesBurned.toTotalCaloriesBurnedToJson(id: Int): TotalCaloriesBurnedToJson =
    TotalCaloriesBurnedToJson(
        id = id,
        calories = calories,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "blood_glucose", indices = [Index(value = ["record_id"], unique = true)])
data class BloodGlucose(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "blood_glucose")
    val bloodGlucose: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class BloodGlucoseToJson(
    val id: Int = 0,
    val bloodGlucose: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun BloodGlucose.toBloodGlucoseToJson(id: Int): BloodGlucoseToJson =
    BloodGlucoseToJson(
        id = id,
        bloodGlucose = bloodGlucose,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "blood_pressure", indices = [Index(value = ["record_id"], unique = true)])
data class BloodPressure(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "systolic")
    val systolic: Double,
    @ColumnInfo(name = "diastolic")
    val diastolic: Double,
    @ColumnInfo(name = "measurement_location")
    val measurementLocation: String,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class BloodPressureToJson(
    val id: Int = 0,
    val systolic: Double,
    val diastolic: Double,
    val measurementLocation: String,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun BloodPressure.toBloodPressureToJson(id: Int): BloodPressureToJson =
    BloodPressureToJson(
        id = id,
        systolic = systolic,
        diastolic = diastolic,
        measurementLocation = measurementLocation,
        date = date,
        startTime = startTime,
        endTime = endTime
    )


@Entity(tableName = "heart_rate", indices = [Index(value = ["record_id"], unique = true)])
data class HeartRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "heart_rate_bpm")
    val heartRate: Long,
    @ColumnInfo(name = "instant")
    val instant: Long,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class HeartRateToJson(
    val id: Int = 0,
    val heartRateBpm: Long,
    val instant: Long,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun HeartRate.toHeartRateToJson(id: Int): HeartRateToJson =
    HeartRateToJson(
        id = id,
        heartRateBpm = heartRate,
        instant = instant,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "body_fat", indices = [Index(value = ["record_id"], unique = true)])
data class BodyFat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "body_fat_percentage")
    val bodyFat: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class BodyFatToJson(
    val id: Int = 0,
    val bodyFatPercentage: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun BodyFat.toBodyFatToJson(id: Int): BodyFatToJson =
    BodyFatToJson(
        id = id,
        bodyFatPercentage = bodyFat,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "body_water_mass", indices = [Index(value = ["record_id"], unique = true)])
data class BodyWaterMass(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "body_water_mass_kg")
    val bodyWaterMass: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class BodyWaterMassToJson(
    val id: Int = 0,
    val bodyWaterMassKg: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun BodyWaterMass.toBodyWaterMassToJson(id: Int): BodyWaterMassToJson =
    BodyWaterMassToJson(
        id = id,
        bodyWaterMassKg = bodyWaterMass,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "bone_mass", indices = [Index(value = ["record_id"], unique = true)])
data class BoneMass(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "bone_mass_kg")
    val boneMass: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class BoneMassToJson(
    val id: Int = 0,
    val boneMassKg: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun BoneMass.toBoneMassToJson(id: Int): BoneMassToJson =
    BoneMassToJson(
        id = id,
        boneMassKg = boneMass,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "distance", indices = [Index(value = ["record_id"], unique = true)])
data class Distance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "distance_m")
    val distance: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class DistanceToJson(
    val id: Int = 0,
    val distanceM: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun Distance.toDistanceToJson(id: Int): DistanceToJson =
    DistanceToJson(
        id = id,
        distanceM = distance,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "elevation_gained")
data class ElevationGained(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "elevation_gained_m")
    val elevationGained: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class ElevationGainedToJson(
    val id: Int = 0,
    val elevationGainedM: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun ElevationGained.toElevationGainedToJson(id: Int): ElevationGainedToJson =
    ElevationGainedToJson(
        id = id,
        elevationGainedM = elevationGained,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "floors_climbed", indices = [Index(value = ["record_id"], unique = true)])
data class FloorsClimbed(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "floors_climbed")
    val floorsClimbed: Int,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class FloorsClimbedToJson(
    val id: Int = 0,
    val floorsClimbed: Int,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun FloorsClimbed.toFloorsClimbedToJson(id: Int): FloorsClimbedToJson =
    FloorsClimbedToJson(
        id = id,
        floorsClimbed = floorsClimbed,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "oxygen_saturation", indices = [Index(value = ["record_id"], unique = true)])
data class OxygenSaturation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "oxygen_saturation_percent")
    val oxygenSaturation: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class OxygenSaturationToJson(
    val id: Int = 0,
    val oxygenSaturationPercent: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun OxygenSaturation.toOxygenSaturationToJson(id: Int): OxygenSaturationToJson =
    OxygenSaturationToJson(
        id = id,
        oxygenSaturationPercent = oxygenSaturation,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "respiratory_rate", indices = [Index(value = ["record_id"], unique = true)])
data class RespiratoryRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "respiratory_rate_bpm")
    val respiratoryRate: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class RespiratoryRateToJson(
    val id: Int = 0,
    val respiratoryRateBpm: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun RespiratoryRate.toRespiratoryRateToJson(id: Int): RespiratoryRateToJson =
    RespiratoryRateToJson(
        id = id,
        respiratoryRateBpm = respiratoryRate,
        date = date,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "sleep_session", indices = [Index(value = ["record_id"], unique = true)])
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "record_id")
    val recordId: String,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "duration")
    val duration: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class SleepSessionToJson(
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
)

fun SleepSession.toSleepSessionToJson(id: Int): SleepSessionToJson =
    SleepSessionToJson(
        id = id,
        startTime = startTime,
        endTime = endTime
    )

@Entity(tableName = "weight", indices = [Index(value = ["records_id"], unique = true)])
data class Weight(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "records_id")
    val recordsId: String,
    @ColumnInfo(name = "weight_grams")
    val weight: Double,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class WeightToJson(
    val id: Int,
    val weightKG: Double,
    val date: Long,
    val startTime: Long,
    val endTime: Long
)

fun Weight.toWeightToJson(id: Int = 0): WeightToJson =
    WeightToJson(
        id = id,
        weightKG = weight,
        date = date,
        startTime = startTime,
        endTime = endTime
    )