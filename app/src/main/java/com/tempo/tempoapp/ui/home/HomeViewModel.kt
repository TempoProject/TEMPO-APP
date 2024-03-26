package com.tempo.tempoapp.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.workers.SaveStepsRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class HomeViewModel(
    bleedingRepository: BleedingRepository,
    infusionRepository: InfusionRepository,
    stepsRecordRepository: StepsRecordRepository,
    private val healthConnectManager: HealthConnectManager,
    private val workManager: WorkManager
) : ViewModel() {

    val permission = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    var permissionsGranted = mutableStateOf(false)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    init {
        initialLoad()
    }

    fun initialLoad() {
        viewModelScope.launch {
            permissionsGranted.value =
                healthConnectManager.hasAllPermissions(permissions = permission)
        }.invokeOnCompletion {
            if (permissionsGranted.value) {
                val task =
                    PeriodicWorkRequest.Builder(SaveStepsRecord::class.java, 15, TimeUnit.MINUTES)
                        .build()
                workManager.enqueueUniquePeriodicWork(
                    "getStepsRecord",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    task
                )
            }
        }
    }

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
            )
        ) { bleeding, infusion, steps ->
            HomeUiState(bleeding, infusion, steps)
        }.stateIn(
            scope = viewModelScope,
            initialValue = HomeUiState(),
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
        )
    /* bleedingRepository.getAll().combie(
         infusionRepository.getAll(),
         stepsRecordRepository.getAll()
     ) { bleeding, infusion, steps ->
         HomeUiState(bleeding, infusion, steps)
     }.stateIn(
         scope = viewModelScope,
         initialValue = HomeUiState(),
         started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS)
     )


    suspend fun readStepsInterval() {
        val startOfDay = Instant.now().minusSeconds(900)
        val now = Instant.now()
        println(healthConnectManager.readSteps(startOfDay, now).forEach {
            println(it.count)
        })
    }
     */

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(
    val bleedingList: List<BleedingEvent> = listOf(),
    val infusionList: List<InfusionEvent> = mutableListOf(),
    val stepsCount: Int = 0
    //val stepsList: List<com.tempo.tempoapp.data.model.StepsRecord> = mutableListOf()
)