package com.tempo.tempoapp.data.healthconnect

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

/**
 * Minimum supported SDK version required for HealthConnect functionality.
 */
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Manager class for interacting with HealthConnect functionalities.
 *
 * @property context The application context.
 */
class HealthConnectManager(private val context: Context) {
    // Lazy initialization of HealthConnectClient instance
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // Mutable state representing HealthConnect availability
    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    /**
     * Checks the availability of HealthConnect functionality.
     * Updates the availability state accordingly.
     */
    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    /**
     * Checks if the current SDK version is supported.
     *
     * @return True if the SDK version is supported, false otherwise.
     */
    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    /**
     * Checks if the HealthConnect client has all the required permissions.
     *
     * @param permissions Set of permissions to check.
     * @return True if all permissions are granted, false otherwise.
     */
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    /**
     * Provides an ActivityResultContract for requesting permissions.
     *
     * @return ActivityResultContract for requesting permissions.
     */
    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * Reads steps records within the specified time range.
     *
     * @param startTime The start time of the time range.
     * @param endTime The end time of the time range.
     * @return A list of steps records.
     */

    suspend fun readSteps(
        startTime: Instant,
        endTime: Instant
    ): List<StepsRecord> {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun totalCaloriesBurned(
        startTime: Instant,
        endTime: Instant
    ): List<TotalCaloriesBurnedRecord> {
        val request = ReadRecordsRequest(
            recordType = TotalCaloriesBurnedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun bloodGlucose(
        startTime: Instant,
        endTime: Instant
    ): List<BloodGlucoseRecord> {
        val request = ReadRecordsRequest(
            recordType = BloodGlucoseRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun heartRate(
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateRecord> {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun bloodPressure(
        startTime: Instant,
        endTime: Instant
    ): List<BloodPressureRecord> {
        val request = ReadRecordsRequest(
            recordType = BloodPressureRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun bodyFat(
        startTime: Instant,
        endTime: Instant
    ): List<BodyFatRecord> {
        val request = ReadRecordsRequest(
            recordType = BodyFatRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun bodyWaterMass(
        startTime: Instant,
        endTime: Instant
    ): List<BodyWaterMassRecord> {
        val request = ReadRecordsRequest(
            recordType = BodyWaterMassRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun boneMass(
        startTime: Instant,
        endTime: Instant
    ): List<BoneMassRecord> {
        val request = ReadRecordsRequest(
            recordType = BoneMassRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun distanceRecords(
        startTime: Instant,
        endTime: Instant
    ): List<DistanceRecord> {
        val request = ReadRecordsRequest(
            recordType = DistanceRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }


    suspend fun elevationGained(
        startTime: Instant,
        endTime: Instant
    ): List<ElevationGainedRecord> {
        val request = ReadRecordsRequest(
            recordType = ElevationGainedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun floorsClimbed(
        startTime: Instant,
        endTime: Instant
    ): List<FloorsClimbedRecord> {
        val request = ReadRecordsRequest(
            recordType = FloorsClimbedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun oxygenSaturation(
        startTime: Instant,
        endTime: Instant
    ): List<OxygenSaturationRecord> {
        val request = ReadRecordsRequest(
            recordType = OxygenSaturationRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun respiratoryRate(
        startTime: Instant,
        endTime: Instant
    ): List<RespiratoryRateRecord> {
        val request = ReadRecordsRequest(
            recordType = RespiratoryRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun sleepSessions(
        startTime: Instant,
        endTime: Instant
    ): List<SleepSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    suspend fun weightRecord(
        startTime: Instant,
        endTime: Instant
    ): List<WeightRecord> {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }
}

/**
 * Enum representing the availability status of HealthConnect functionality.
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}