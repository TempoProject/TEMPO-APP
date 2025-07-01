package com.tempo.tempoapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.model.ProphylaxisResponse
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.data.repository.ProphylaxisResponseRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.temporal.ChronoUnit

/** ViewModel for the Home screen.
 *
 * @param bleedingRepository Repository for bleeding events.
 * @param infusionRepository Repository for infusion events.
 * @param stepsRecordRepository Repository for steps records.
 * @param movesenseRepository Repository for Movesense device information.
 */

class HomeViewModel(
    bleedingRepository: BleedingRepository,
    infusionRepository: InfusionRepository,
    stepsRecordRepository: StepsRecordRepository,
    prophylaxisResponseRepository: ProphylaxisResponseRepository,
    movesenseRepository: MovesenseRepository,
    //private val healthConnectManager: HealthConnectManager,
    //application: Application
) : ViewModel() {

    /**
     * Set of permissions required for reading steps records.
     */
    //val permission = setOf(
    //  HealthPermission.getReadPermission(StepsRecord::class),
    /*HealthPermission.getReadPermission(HeartRateRecord::class),
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
    HealthPermission.getReadPermission(WeightRecord::class)*/
    //)


    //var permissionsGranted = mutableStateOf(false)
    //   private set

    //val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    init {

    }

    /**
     * Initial load function to check for permissions and schedule an alarm if permissions are granted.
     */
    fun initialLoad() {
        /*viewModelScope.launch {
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
        }*/
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
            prophylaxisResponseRepository.getAllDayProphylaxis(
                Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
            ),
            movesenseRepository.getDevice()
        ) { bleeding, infusion, steps, prophylaxis, movesense ->
            println(movesense)
            Log.d("HomeViewModel", Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli().toString())
            Log.d("HomeViewModel", "Prophylaxis events: $prophylaxis")
            HomeUiState(bleeding, infusion, prophylaxis, steps, movesense)
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
    val infusionList: List<InfusionEvent> = listOf(),
    val prophylaxisList: List<ProphylaxisResponse> = listOf(),
    val stepsCount: Int = 0,
    val movesense: Movesense? = null
) {
    val combinedEvents: List<HomeEvent>
        get() = (bleedingList.map { HomeEvent.Bleeding(it) } +
                infusionList.map { HomeEvent.Infusion(it) }) +
                prophylaxisList.map { HomeEvent.Prophylaxis(it) }
                    .sortedByDescending { it.dateTime }
}

sealed class HomeEvent(val dateTime: Long) {
    data class Bleeding(val event: BleedingEvent) : HomeEvent(event.timestamp)
    data class Infusion(val event: InfusionEvent) : HomeEvent(event.timestamp)
    data class Prophylaxis(val event: ProphylaxisResponse) : HomeEvent(event.reminderDateTime)
}
