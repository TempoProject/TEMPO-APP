package com.tempo.tempoapp.ui.onboarding

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.workers.GetStepsRecord
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object ProphylaxisScreen : NavigationDestination {
    override val route: String = "prophylaxis"
    override val titleRes: Int = R.string.profylaxis_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProphylaxisScreen(
    navController: NavController? = null,
) {
    val viewModel: ProphylaxisViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.loadSavedConfig()
    }
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }

    val drugNameFocusRequester = remember { FocusRequester() }
    val dosageFocusRequester = remember { FocusRequester() }
    val drugNameExtraFocusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profylaxis_screen_title),
                    )
                }
            )
        }
    ) { padding ->

        if (showTimePicker) {
            TimePickerDialog(
                context = context,
                initialTime = uiState.reminderTime,
                onTimeSelected = {
                    viewModel.updateReminderTime(it)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        if (showDatePicker && uiState.schedulingMode == SchedulingMode.Recurring) {
            DatePickerDialog(
                initialDate = uiState.startDate ?: LocalDate.now(),
                onDateSelected = {
                    viewModel.updateStartDate(it)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Tipo di schedulazione:")
                    Row {
                        FilterChip(
                            selected = uiState.schedulingMode == SchedulingMode.DaysOfWeek,
                            onClick = { viewModel.onSchedulingModeChange(SchedulingMode.DaysOfWeek) },
                            label = { Text("Giorni della settimana") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.schedulingMode == SchedulingMode.Recurring,
                            onClick = { viewModel.onSchedulingModeChange(SchedulingMode.Recurring) },
                            label = { Text("Ogni N giorni/settimane") }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    when (uiState.schedulingMode) {
                        SchedulingMode.DaysOfWeek -> {
                            Text("Seleziona il giorno:")
                            WeekdaySelector(
                                selectedDays = uiState.selectedDays,
                                onDayToggle = viewModel::onDayToggle
                            )
                            if (uiState.selectedDaysError) {
                                Text(
                                    text = "Seleziona almeno un giorno",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        SchedulingMode.Recurring -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = uiState.recurrenceIntervalText,
                                    onValueChange = viewModel::onRecurrenceIntervalTextChange,
                                    label = { Text("Ogni") },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier.width(100.dp),
                                    isError = uiState.recurrenceIntervalError,
                                    supportingText = if (uiState.recurrenceIntervalError) {
                                        { Text("Campo richiesto") }
                                    } else null
                                )
                                Spacer(Modifier.width(8.dp))
                                DropdownMenuUnit(
                                    selectedUnit = uiState.recurrenceUnit,
                                    onSelect = viewModel::onRecurrenceUnitChange
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text("Data di inizio ricorrenza:")
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = uiState.startDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Ora promemoria:")
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = uiState.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = uiState.drugName,
                        onValueChange = viewModel::onDrugNameChange,
                        label = { Text("Nome farmaco profilassi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(drugNameFocusRequester),
                        isError = uiState.drugNameError,
                        supportingText = if (uiState.drugNameError) {
                            { Text("Campo richiesto") }
                        } else null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { dosageFocusRequester.requestFocus() }
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.dosage,
                        onValueChange = viewModel::onDosageChange,
                        label = { Text("Dosaggio profilassi (mg/kg)") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { drugNameExtraFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(dosageFocusRequester),
                        isError = uiState.dosageError,
                        supportingText = if (uiState.dosageError) {
                            { Text("Campo richiesto") }
                        } else null
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.drugNameExtra,
                        onValueChange = viewModel::onDrugExtraInfusionNameChange,
                        label = { Text("Inserisci farmaco infusioni on-demand") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(drugNameExtraFocusRequester),
                        isError = uiState.drugNameExtraError,
                        supportingText = if (uiState.drugNameExtraError) {
                            { Text("Campo richiesto") }
                        } else null
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager =
                                    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    showExactAlarmPermissionDialog =
                                        !alarmManager.canScheduleExactAlarms()
                                } else {
                                    viewModel.saveProphylaxis(context) {
                                        val workManager = WorkManager.getInstance(context)
                                        workManager.enqueueUniquePeriodicWork(
                                            "GetStepsRecord",
                                            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                            PeriodicWorkRequestBuilder<GetStepsRecord>(15, TimeUnit.MINUTES)
                                                .build()
                                        )

                                        navController?.navigate(HomeDestination.route) {
                                            popUpTo(ProphylaxisScreen.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Conferma profilassi")
                    }
                }
            }
        }
    }

    if (showExactAlarmPermissionDialog) {
        ExactAlarmPermissionDialog(onConfirm = {
            val intent =
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                }
            context.startActivity(intent)
            showExactAlarmPermissionDialog = false
        }, { showExactAlarmPermissionDialog = false })
    }
}

enum class SchedulingMode { DaysOfWeek, Recurring }
enum class RecurrenceUnit { Days, Weeks }

@Composable
fun WeekdaySelector(
    selectedDays: DayOfWeek?,
    onDayToggle: (DayOfWeek) -> Unit
) {
    val days = DayOfWeek.entries.toTypedArray()
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        days.forEach { day ->
            FilterChip(
                selected = selectedDays == day,
                onClick = { onDayToggle(day) },
                label = { Text(day.name.take(1)) }
            )
        }
    }
}

@Composable
fun DropdownMenuUnit(
    selectedUnit: RecurrenceUnit,
    onSelect: (RecurrenceUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedUnit.name.lowercase().replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RecurrenceUnit.entries.forEach {
                DropdownMenuItem(text = { Text(it.name.lowercase()) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeSelected(LocalTime.of(hour, minute))
            },
            initialTime.hour,
            initialTime.minute,
            true
        )
    }

    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose { timePickerDialog.dismiss() }
    }
}

@Composable
fun ExactAlarmPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Autorizzazione necessaria") },
        text = {
            Text(
                "Per garantire la corretta esecuzione dei promemoria programmati, " +
                        "l'app richiede il permesso di impostare allarmi precisi. " +
                        "Premi 'Continua' per concedere l'autorizzazione."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Continua")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose { datePickerDialog.dismiss() }
    }
}

@Preview
@Composable
fun ProphylaxisScreenPreview() {
    ProphylaxisScreen(

    )
}