package com.tempo.tempoapp.ui.onboarding

import AppPreferencesManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.utils.ProphylaxisScheduler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


class ProphylaxisViewModel(val appPreferencesManager: AppPreferencesManager) : ViewModel() {
    var uiState = mutableStateOf(ProphylaxisUiState())
        private set


    private fun updateUiState(update: (ProphylaxisUiState) -> ProphylaxisUiState) {
        uiState.value = update(uiState.value)
    }

    fun loadSavedConfig() {
        updateUiState { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val config = appPreferencesManager.prophylaxisConfig.firstOrNull()

                config?.let { savedConfig ->
                    updateUiState { currentState ->
                        currentState.copy(
                            schedulingMode = savedConfig.schedulingMode,
                            selectedDays = savedConfig.selectedDays,
                            isActiveProphylaxis = savedConfig.isActive,
                            recurrenceIntervalText = savedConfig.recurrenceInterval.toString(),
                            recurrenceUnit = savedConfig.recurrenceUnit,
                            reminderTime = LocalTime.of(savedConfig.hour, savedConfig.minute),
                            startDate = savedConfig.startDate ?: LocalDate.now(),
                            drugName = savedConfig.drugName,
                            dosage = savedConfig.dosage,
                            dosageUnit = DosageUnit.valueOf(savedConfig.dosageUnit!!),
                            drugNameExtra = savedConfig.drugNameExtra,
                            isLoading = false
                        )
                    }
                    Log.d("ViewModel", "Configurazione caricata: $savedConfig")
                } ?: run {
                    updateUiState { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Errore durante il caricamento", e)
                updateUiState { it.copy(isLoading = false) }
            }
        }
    }

    fun updateReminderTime(time: LocalTime) {
        updateUiState { it.copy(reminderTime = time) }
        Log.d("ViewModel", "Ora promemoria aggiornata a: $time")
    }

    fun updateStartDate(date: LocalDate) {
        updateUiState { it.copy(startDate = date) }
        Log.d("ViewModel", "Data di inizio aggiornata a: $date")
    }

    fun onSchedulingModeChange(mode: SchedulingMode) {
        updateUiState {
            it.copy(
                schedulingMode = mode,
                selectedDaysError = false
            )
        }
        Log.d("ViewModel", "Modalità di scheduling cambiata a: $mode")
    }

    fun onDayToggle(day: DayOfWeek) {
        val newSelectedDays = if (uiState.value.selectedDays == day) null else day
        updateUiState {
            it.copy(
                selectedDays = newSelectedDays,
                selectedDaysError = false
            )
        }
        Log.d("ViewModel", "Giorno selezionato: $newSelectedDays")
    }

    fun onRecurrenceUnitChange(unit: RecurrenceUnit) {
        updateUiState { it.copy(recurrenceUnit = unit) }
        Log.d("ViewModel", "Unità di ricorrenza cambiata a: $unit")
    }

    fun onRecurrenceIntervalTextChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
            updateUiState {
                it.copy(
                    recurrenceIntervalText = value,
                    recurrenceIntervalError = false
                )
            }
            Log.d("ViewModel", "Testo intervallo di ricorrenza cambiato a: '$value'")
        }
    }

    fun onDrugNameChange(name: String) {
        updateUiState {
            it.copy(
                drugName = name,
                drugNameError = false
            )
        }
    }

    fun onDrugExtraInfusionNameChange(name: String) {
        updateUiState {
            it.copy(
                drugNameExtra = name,
                drugNameExtraError = false
            )
        }
    }

    fun onDosageChange(value: String) {
        updateUiState {
            it.copy(
                dosage = value,
                dosageError = false
            )
        }
    }

    fun onDosageUnitChange(unit: DosageUnit) {
        updateUiState { it.copy(dosageUnit = unit) }
        Log.d("ViewModel", "Unità di dosaggio cambiata a: $unit")
    }

    private fun validateFields(): Boolean {
        val currentState = uiState.value

        val drugNameError = currentState.drugName.isBlank()
        val dosageError = currentState.dosage.isBlank()
        val drugNameExtraError = currentState.drugNameExtra.isBlank()
        val recurrenceIntervalError = currentState.schedulingMode == SchedulingMode.Recurring &&
                currentState.recurrenceIntervalText.isEmpty()
        val selectedDaysError = currentState.schedulingMode == SchedulingMode.DaysOfWeek &&
                currentState.selectedDays == null

        updateUiState {
            it.copy(
                drugNameError = drugNameError,
                dosageError = dosageError,
                drugNameExtraError = drugNameExtraError,
                recurrenceIntervalError = recurrenceIntervalError,
                selectedDaysError = selectedDaysError
            )
        }

        return !drugNameError && !dosageError && !drugNameExtraError &&
                !recurrenceIntervalError && !selectedDaysError
    }

    fun saveProphylaxis(context: Context, onComplete: () -> Unit) {
        Log.d("ViewModel", "Salvataggio profilassi con ora: ${uiState.value.reminderTime}")

        if (!validateFields()) {
            Log.w("ViewModel", "Validazione fallita")
            return
        }

        viewModelScope.launch {
            val currentState = uiState.value

            appPreferencesManager.saveProphylaxisConfig(
                schedulingMode = currentState.schedulingMode,
                selectedDays = currentState.selectedDays,
                recurrenceInterval = currentState.recurrenceInterval,
                recurrenceUnit = currentState.recurrenceUnit,
                startDate = currentState.startDate,
                hour = currentState.reminderTime.hour,
                minute = currentState.reminderTime.minute,
                drugName = currentState.drugName,
                dosage = currentState.dosage,
                dosageUnit = currentState.dosageUnit,
                drugNameExtra = currentState.drugNameExtra
            )

            appPreferencesManager.setActiveProphylaxis(true)

            val scheduler = ProphylaxisScheduler(context)
            scheduler.cancelAllAlarms()

            when (currentState.schedulingMode) {
                SchedulingMode.DaysOfWeek -> {
                    currentState.selectedDays?.let { day ->
                        Log.d(
                            "ViewModel",
                            "Programmazione settimanale per giorno: $day alle ${currentState.reminderTime.hour}:${currentState.reminderTime.minute}"
                        )
                        scheduler.scheduleWeeklyAlarms(
                            selectedDays = setOf(day),
                            hour = currentState.reminderTime.hour,
                            minute = currentState.reminderTime.minute
                        )
                    }
                }

                SchedulingMode.Recurring -> {
                    Log.d(
                        "ViewModel",
                        "Programmazione ricorrente ogni ${currentState.recurrenceInterval} ${currentState.recurrenceUnit} dalle ${currentState.startDate} alle ${currentState.reminderTime.hour}:${currentState.reminderTime.minute}"
                    )
                    scheduler.scheduleRecurringAlarm(
                        interval = currentState.recurrenceInterval,
                        unit = currentState.recurrenceUnit,
                        startDate = currentState.startDate!!,
                        hour = currentState.reminderTime.hour,
                        minute = currentState.reminderTime.minute
                    )
                }
            }

            onComplete()
        }
    }
}

data class ProphylaxisUiState(
    // TODO CHECK VALORI CONSENTITI

    val isLoading: Boolean = false,
    val isActiveProphylaxis: Boolean = false,
    val schedulingMode: SchedulingMode = SchedulingMode.DaysOfWeek,
    val selectedDays: DayOfWeek? = null,
    val recurrenceUnit: RecurrenceUnit = RecurrenceUnit.Days,
    val recurrenceIntervalText: String = "1",
    val startDate: LocalDate? = LocalDate.now(),
    val drugName: String = "",
    val dosage: String = "",
    val dosageUnit: DosageUnit = DosageUnit.MG_KG,
    val reminderTime: LocalTime = LocalTime.of(8, 0),
    val drugNameExtra: String = "",

    // Stati di errore
    val drugNameError: Boolean = false,
    val dosageError: Boolean = false,
    val recurrenceIntervalError: Boolean = false,
    val drugNameExtraError: Boolean = false,
    val selectedDaysError: Boolean = false
) {
    val recurrenceInterval: Int
        get() = recurrenceIntervalText.toIntOrNull()?.coerceAtLeast(1) ?: 1

    // Inutilizzato
    val dosageUnitText: String
        get() = when (dosageUnit) {
            DosageUnit.MG_KG -> "mg/kg"
            DosageUnit.IU -> "IU"
        }

    // inutilizzato
    val isValid: Boolean
        get() = !drugNameError && !dosageError && !recurrenceIntervalError &&
                !drugNameExtraError && !selectedDaysError
}