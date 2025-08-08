package com.tempo.tempoapp.ui.prophylaxis

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.InformationDialog
import com.tempo.tempoapp.ui.filterDoseInput
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ProphylaxisScreen : NavigationDestination {
    override val route: String = "prophylaxis"
    override val titleRes: Int = R.string.profylaxis_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProphylaxisScreen(
    navController: NavController? = null,
) {
    val context = LocalContext.current
    val viewModel: ProphylaxisViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.loadSavedConfig()
    }


    val focusManager = LocalFocusManager.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }

    val drugNameFocusRequester = remember { FocusRequester() }
    val dosageFocusRequester = remember { FocusRequester() }
    val drugNameExtraFocusRequester = remember { FocusRequester() }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon =
                    {
                        if (uiState.isActiveProphylaxis)
                            IconButton(
                                onClick = { showExitDialog = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_arrow_back_24),
                                    contentDescription = null
                                )
                            }
                    },
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
                    Log.d("ProphylaxisScreen", "Selected date: $it")
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
                    Text(stringResource(R.string.scheduling_type))
                    Row {
                        FilterChip(
                            selected = uiState.schedulingMode == SchedulingMode.DaysOfWeek,
                            onClick = { viewModel.onSchedulingModeChange(SchedulingMode.DaysOfWeek) },
                            label = { Text(stringResource(R.string.days_of_week)) }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = uiState.schedulingMode == SchedulingMode.Recurring,
                            onClick = { viewModel.onSchedulingModeChange(SchedulingMode.Recurring) },
                            label = { Text(stringResource(R.string.custom)) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    when (uiState.schedulingMode) {
                        SchedulingMode.DaysOfWeek -> {
                            Text(stringResource(R.string.select_day))
                            WeekdaySelector(
                                selectedDays = uiState.selectedDays,
                                onDayToggle = viewModel::onDayToggle
                            )
                            if (uiState.selectedDaysError) {
                                Text(
                                    text = stringResource(R.string.select_at_least_one_day),
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
                                    label = { Text(stringResource(R.string.every)) },
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
                                        { Text(stringResource(R.string.required_field)) }
                                    } else null
                                )
                                Spacer(Modifier.width(8.dp))
                                DropdownMenuUnit(
                                    selectedUnit = uiState.recurrenceUnit,
                                    onSelect = viewModel::onRecurrenceUnitChange
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(stringResource(R.string.recurrence_start_date))
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = uiState.startDate!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(stringResource(R.string.reminder_time))
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
                        label = { Text(stringResource(R.string.prophylaxis_drug_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(drugNameFocusRequester),
                        isError = uiState.drugNameError,
                        supportingText = if (uiState.drugNameError) {
                            { Text(stringResource(R.string.required_field)) }
                        } else null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { dosageFocusRequester.requestFocus() }
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                    ) {
                        OutlinedTextField(
                            value = uiState.dosage,
                            onValueChange = { viewModel.onDosageChange(filterDoseInput(it)) },
                            label = { Text(stringResource(R.string.prophylaxis_dosage)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { drugNameExtraFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(dosageFocusRequester),
                            isError = uiState.dosageError,
                            supportingText = if (uiState.dosageError) {
                                { Text(stringResource(R.string.required_field)) }
                            } else null
                        )
                        DropdownMenuDosageUnit(
                            selectedUnit = uiState.dosageUnit,
                            onSelect = viewModel::onDosageUnitChange,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.drugNameExtra,
                        onValueChange = viewModel::onDrugExtraInfusionNameChange,
                        label = { Text(stringResource(R.string.on_demand_infusion_drug)) },
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
                            { Text(stringResource(R.string.required_field)) }
                        } else null
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val canScheduleAlarms =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    // Android 12+ - controlla permessi exact alarms
                                    val alarmManager =
                                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    alarmManager.canScheduleExactAlarms()
                                } else {
                                    // Android 11 e precedenti - exact alarms sempre disponibili
                                    true
                                }
                            if (!canScheduleAlarms) {
                                showExactAlarmPermissionDialog = true
                            } else {
                                viewModel.saveProphylaxis(context) {
                                    TempoApplication.startHealthConnectWorkManager(context)
                                    TempoApplication.updateSessionID(context)
                                    TempoApplication.startServerSyncWorkManagers(context)
                                    /*val workManager = WorkManager.getInstance(context)
                                    workManager.enqueueUniquePeriodicWork(
                                        "GetStepsRecord",
                                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                        PeriodicWorkRequestBuilder<GetStepsRecord>(
                                            15,
                                            TimeUnit.MINUTES
                                        )
                                            .build()
                                    )
*/
                                    navController?.navigate(HomeDestination.route) {
                                        popUpTo(ProphylaxisScreen.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.confirm_prophylaxis))
                    }
                }

            }
        }
        if (showExitDialog) {
            InformationDialog(
                onConfirm = {
                    showExitDialog = false
                    navController?.navigateUp()
                },
                onDismiss = {
                    showExitDialog = false
                }
            )
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
            val recurrence = when (selectedUnit) {
                RecurrenceUnit.Days -> stringResource(R.string.days)
                RecurrenceUnit.Weeks -> stringResource(R.string.weeks)
            }
            Text(recurrence.replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RecurrenceUnit.entries.forEach {
                when (it) {
                    RecurrenceUnit.Days -> {
                        DropdownMenuItem(text = { Text("Giorni") }, onClick = {
                            onSelect(it)
                            expanded = false
                        })
                    }

                    RecurrenceUnit.Weeks -> {
                        DropdownMenuItem(text = { Text("Settimane") }, onClick = {
                            onSelect(it)
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuDosageUnit(
    selectedUnit: DosageUnit,
    onSelect: (DosageUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.width(80.dp)
        ) {
            Text(
                text = when (selectedUnit) {
                    DosageUnit.MG_KG -> "mg/kg"
                    DosageUnit.IU -> "IU"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DosageUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (unit) {
                                DosageUnit.MG_KG -> "mg/kg"
                                DosageUnit.IU -> "IU (International Units)"
                            }
                        )
                    },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    }
                )
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
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeSelected(LocalTime.of(hour, minute))
            },
            initialTime.hour,
            initialTime.minute,
            true
        )
    }.apply {
        setOnDismissListener { onDismiss() }
        setOnCancelListener { onDismiss() }
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
        title = { Text(stringResource(R.string.confirm_prophylaxis)) },
        text = {
            Text(
                stringResource(R.string.alarm_permission_message)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.continue_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
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
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            setOnDismissListener { onDismiss() }
            setOnCancelListener { onDismiss() }
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