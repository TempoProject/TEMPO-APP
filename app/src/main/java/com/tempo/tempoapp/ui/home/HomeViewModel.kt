package com.tempo.tempoapp.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.healthconnect.HealthConnectAvailability
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.repository.BleedingRepository
import com.tempo.tempoapp.data.repository.InfusionRepository
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.data.repository.StepsRecordRepository
import com.tempo.tempoapp.utils.StepsReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class HomeViewModel(
    bleedingRepository: BleedingRepository,
    infusionRepository: InfusionRepository,
    stepsRecordRepository: StepsRecordRepository,
    movesenseRepository: MovesenseRepository,
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
        if (healthConnectManager.availability.value == HealthConnectAvailability.INSTALLED)
            initialLoad()
    }

    fun initialLoad() {
        viewModelScope.launch {
            permissionsGranted.value =
                healthConnectManager.hasAllPermissions(permissions = permission)
            println("permission granted in viewmodel? ${permissionsGranted.value}")
        }.invokeOnCompletion {
            if (permissionsGranted.value) {
                val instant = Instant.now().toEpochMilli()
                val intent =
                    Intent(TempoApplication.instance.applicationContext, StepsReceiver::class.java)
                intent.putExtra("instant", instant)
                val pendingIntent = PendingIntent.getBroadcast(
                    TempoApplication.instance.applicationContext,
                    instant.toInt(),
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (TempoApplication.instance.alarm.canScheduleExactAlarms()) {
                        println("scheduling")
                        TempoApplication.instance.alarm.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            instant,
                            pendingIntent
                        )
                    }
                } else
                    TempoApplication.instance.alarm.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        instant,
                        pendingIntent
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
    val stepsCount: Int = 0,
    val movesense: Movesense? = null
)