package com.tempo.tempoapp.ui.reminders

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.data.model.events
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.bleeding.DatePickerDialog
import com.tempo.tempoapp.ui.bleeding.TextWithIcon
import com.tempo.tempoapp.ui.bleeding.TimePickerDialog
import com.tempo.tempoapp.ui.common.CalendarTextProvider
import com.tempo.tempoapp.ui.common.PermissionDialog
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Representing a navigation destination for reminders.
 */
object ReminderDestination : NavigationDestination {
    override val route: String
        get() = "reminder"
    override val titleRes: Int
        get() = R.string.reminder
}

/**
 * ReminderScreen is a composable function that represents the UI screen for managing reminders.
 *
 * @param viewModel The view model for managing reminder functionality.
 * @param lifecycleOwner The lifecycle owner used for observing the lifecycle events.
 * @param onNavigateUp Callback function to navigate up.
 * @param navigateBack Callback function to navigate back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = viewModel(factory = AppViewModelProvider.Factory),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateUp: () -> Unit,
    navigateBack: () -> Unit
) {

    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(false)
    }
    var hasPermission by remember {
        mutableStateOf(false)
    }
    var isPermissionPermanentlyDenied by remember { mutableStateOf(false) }

    val openSettings =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {}

    val permission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            isPermissionPermanentlyDenied = perms.any {
                !it.value && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    it.key
                )
            }
            hasPermission = perms.values.first() && perms.values.last()
        }

    fun checkPermissions() {

        hasPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            else
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED

        showDialog = !(hasPermission)

    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    DisposableEffect(context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = R.string.reminder),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) {
        ReminderBody(
            modifier = Modifier.padding(it),
            viewModel.uiState,
            viewModel::updateEvent,
            viewModel::updateTime,
            viewModel::updateDate,
            viewModel::updateIsPeriodic,
            viewModel::updateInterval,
            viewModel::updateTimeUnit,
            viewModel::reset,
            onSave = {
                if (hasPermission) {
                    coroutineScope.launch {
                        viewModel.save()
                    }
                    navigateBack()
                } else {
                    showDialog = true
                    Toast.makeText(
                        context,
                        "Concedi l'accesso al calendario",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        )

        if (showDialog) {
            Log.d(
                "ReminderScreen", "permission rationale: ${
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.WRITE_CALENDAR
                    ) && ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.WRITE_CALENDAR
                    )
                }"
            )
            val isPermanentlyDeclined =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.WRITE_CALENDAR
                ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.READ_CALENDAR
                ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) else
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.WRITE_CALENDAR
                    ) /*&& !ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.READ_CALENDAR
                    )*/
            PermissionDialog(
                showDialog,
                permission = CalendarTextProvider(),
                isPermanentlyDeclined = isPermanentlyDeclined,
                onDismiss = { showDialog = !showDialog },
                onOkClick = {
                    val permissionRequired =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) else arrayOf(
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR,
                        )

                    permission.launch(
                        permissionRequired
                    )
                },
                onGoToAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.parse("package:${context.packageName}")
                    intent.setData(uri)
                    openSettings.launch(intent)
                })
        }
    }
}

/**
 * ReminderBody is a composable function representing the main content of the reminder screen.
 *
 * @param modifier Modifier for the layout.
 * @param uiState UI state for the reminder.
 * @param updateEvent Function to update the reminder event.
 * @param updateTime Function to update the reminder time.
 * @param updateDate Function to update the reminder date.
 * @param updateIsPeriodic Function to update whether the reminder is periodic.
 * @param updateInterval Function to update the reminder interval.
 * @param updateTimeUnit Function to update the reminder time unit.
 * @param reset Function to reset the reminder.
 * @param onSave Function to save the reminder.
 */
@Composable
private fun ReminderBody(
    modifier: Modifier = Modifier,
    uiState: ReminderUiState,
    updateEvent: (String) -> Unit,
    updateTime: (String) -> Unit,
    updateDate: (Long) -> Unit,
    updateIsPeriodic: (Boolean) -> Unit,
    updateInterval: (Int) -> Unit,
    updateTimeUnit: (TimeUnit) -> Unit,
    reset: () -> Unit,
    onSave: () -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }
    var showDropdownInterval by remember {
        mutableStateOf(false)
    }

    var showDropdownTimeUnit by remember {
        mutableStateOf(false)
    }
    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }
    var showTimePickerDialog by remember {
        mutableStateOf(false)
    }

    var saveIsEnabled by remember {
        mutableStateOf(false)
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_small))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDropdown = !showDropdown }
                .border(
                    1.dp,
                    Color.Black,
                    RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))
                )
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            TextWithIcon(text = uiState.event)
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = !showDropdown },
                offset = DpOffset(
                    x = dimensionResource(id = R.dimen.padding_small),
                    y = dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                events.forEach { event ->
                    DropdownMenuItem(
                        text = { Text(text = event) },
                        onClick = {
                            updateEvent(event)
                            saveIsEnabled = event != "Tipo di evento"
                            showDropdown = !showDropdown
                        },
                        contentPadding = PaddingValues(8.dp)
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen.padding_small)),
            Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { showDatePickerDialog = !showDatePickerDialog },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier
                    .weight(2f)
            ) {
                Text(text = uiState.date)
                Spacer(modifier = Modifier.padding(1.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.padding(2.dp))

            OutlinedButton(
                onClick = {
                    showTimePickerDialog = !showTimePickerDialog
                },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = uiState.time,
                )
                Icon(
                    painter = painterResource(id = R.drawable.baseline_access_time_24),
                    contentDescription = null
                )
            }

            if (showDatePickerDialog)
                DatePickerDialog(
                    onDateSelected = updateDate,
                    onDismiss = { showDatePickerDialog = !showDatePickerDialog })

            if (showTimePickerDialog)
                TimePickerDialog(
                    onTimeSelected = updateTime,
                    onDismiss = { showTimePickerDialog = !showTimePickerDialog })
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Periodico")
            Checkbox(
                checked = uiState.isPeriodic,
                onCheckedChange = { updateIsPeriodic(!uiState.isPeriodic) })
            Box(
                modifier = Modifier
                    .clickable {
                        showDropdownInterval = !showDropdownInterval && uiState.isPeriodic
                    }
                    .border(
                        1.dp,
                        Color.Black,
                        RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))
                    )
                    .padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(text = uiState.interval.toString())
                DropdownMenu(
                    expanded = showDropdownInterval,
                    onDismissRequest = { showDropdownInterval = !showDropdownInterval },
                    offset = DpOffset(
                        x = dimensionResource(id = R.dimen.padding_small),
                        y = dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    (1..24).filter { it % 2 == 0 }.forEach { step ->
                        DropdownMenuItem(
                            text = { Text(text = step.toString()) },
                            onClick = {
                                updateInterval(step)
                                showDropdownInterval = !showDropdownInterval
                            },
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .clickable {
                        showDropdownTimeUnit = !showDropdownTimeUnit && uiState.isPeriodic
                    }
                    .border(
                        1.dp,
                        Color.Black,
                        RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))
                    )
                    .padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                TextWithIcon(text = uiState.timeUnit.name)
                DropdownMenu(
                    modifier = Modifier.clickable { uiState.isPeriodic },
                    expanded = showDropdownTimeUnit,
                    onDismissRequest = { showDropdownTimeUnit = !showDropdownTimeUnit },
                    offset = DpOffset(
                        x = dimensionResource(id = R.dimen.padding_small),
                        y = dimensionResource(id = R.dimen.padding_small)
                    )
                ) {
                    TimeUnit.entries.drop(6).forEach { step ->
                        DropdownMenuItem(
                            text = { Text(text = step.name) },
                            onClick = {
                                updateTimeUnit(step)
                                showDropdownTimeUnit = !showDropdownTimeUnit
                            },
                            contentPadding = PaddingValues(8.dp)
                        )
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = {
                saveIsEnabled = false
                reset()

            }) {
                Text(text = "Reset")
            }
            OutlinedButton(onClick = onSave, enabled = saveIsEnabled) {
                Text(text = "Salva")
            }

        }

    }
}