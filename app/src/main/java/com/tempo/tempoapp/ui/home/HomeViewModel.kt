package com.tempo.tempoapp.ui.home

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.healthconnect.HealthConnectAvailability
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.utils.AlarmManagerHelper
import com.tempo.tempoapp.utils.StepsReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

/** ViewModel for the Home screen.
 *
 * @param bleedingRepository Repository for bleeding events.
 * @param infusionRepository Repository for infusion events.
 * @param stepsRecordRepository Repository for steps records.
 * @param movesenseRepository Repository for Movesense device information.
 * @param healthConnectManager Health Connect manager.
 * @param application Application context.
 */

class HomeViewModel(
    bleedingRepository: BleedingRepository,
    infusionRepository: InfusionRepository,
    stepsRecordRepository: StepsRecordRepository,
    movesenseRepository: MovesenseRepository,
    private val healthConnectManager: HealthConnectManager,
    application: Application
) : AndroidViewModel(application) {

    /**
     * Set of permissions required for reading steps records.
     */
    val permission = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(BodyWaterMassRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ElevationGainedRecord::class),
        HealthPermission.getReadPermission(FloorsClimbedRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
    )

    /**
     * State to keep track of whether the required permissions are granted.
     */
    var permissionsGranted = mutableStateOf(false)
        private set

    /**
     * Permissions launcher for requesting Health Connect permissions.
     */
    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    init {
        if (healthConnectManager.availability.value == HealthConnectAvailability.INSTALLED)
            initialLoad()
    }

    /**
     * Initial load function to check for permissions and schedule an alarm if permissions are granted.
     */
    fun initialLoad() {
        viewModelScope.launch {
            permissionsGranted.value =
                healthConnectManager.hasAllPermissions(permissions = permission)
            println("permission granted in viewmodel? ${permissionsGranted.value}")
        }.invokeOnCompletion {
            if (permissionsGranted.value) {
                val instant = Instant.now().toEpochMilli()
                val intent =
                    Intent(
                        getApplication<Application>().applicationContext,
                        StepsReceiver::class.java
                    )
                intent.putExtra("instant", instant)
                val pendingIntent = PendingIntent.getBroadcast(
                    getApplication<Application>().applicationContext,
                    instant.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                AlarmManagerHelper(getApplication<Application>().applicationContext).scheduleStepsService(
                    pendingIntent,
                    instant
                )
            }
        }
    }

    /**
     * Combined state flow of UI state containing lists of bleeding events, infusion events,
     * steps count, and movesense device information.
     */
    val homeUiState: StateFlow<HomeUiState> =
        combine(
            bleedingRepository.getAllDayBleeding(
                Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
            ),
            infusionRepository.getAllDayInfusion(
                Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
            ),
            stepsRecordRepository.getAllDayStepsCount(
                Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
            ),
            movesenseRepository.getDevice()
        ) { bleeding, infusion, steps, movesense ->
            println(movesense)
            HomeUiState(bleeding, infusion, steps, movesense)
        }.stateIn(
            scope = viewModelScope,
            initialValue = HomeUiState(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Data class representing the UI state of the Home screen.
 *
 * @param bleedingList List of bleeding events.
 * @param infusionList List of infusion events.
 * @param stepsCount Total steps count.
 * @param movesense Movesense device information.
 */
data class HomeUiState(
    val bleedingList: List<BleedingEvent> = listOf(),
    val infusionList: List<InfusionEvent> = mutableListOf(),
    val stepsCount: Int = 0,
    val movesense: Movesense? = null
)