package com.tempo.tempoapp.ui.prophylaxis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


object ProphylaxisEditDestination : NavigationDestination {
    override val route = "prophylaxis_edit"
    override val titleRes: Int
        get() = R.string.edit_prophylaxis

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProphylaxisEditScreen(
    navController: NavController? = null
) {
    val viewModel: ProphylaxisEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(R.string.edit_prophylaxis),
                canNavigateBack = true,
                navigateUp = { navController?.navigateUp() }
            )
        }) { paddingValues ->
        ProphylaxisEditBody(
            uiState = uiState,
            originalResponseDateTime = viewModel.getOriginalResponseDateTime(),
            onItemClick = viewModel::updateUiState,
            onSave = {
                coroutineScope.launch {
                    val success = viewModel.updateProphylaxisResponse()
                    if (success) {
                        navController?.navigateUp()
                    }
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun ProphylaxisEditBody(
    uiState: ProphylaxisEditUiState,
    originalResponseDateTime: Long,
    onItemClick: (ProphylaxisDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }


    val context = LocalContext.current
    if (uiState.isLoading)
        Loading()


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = stringResource(R.string.prophylaxis_details),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                )

                DetailRow(
                    label = stringResource(R.string.date),
                    value = uiState.prophylaxisDetails.reminderDateTime.toStringDate()
                )

                DetailRow(
                    label = stringResource(R.string.reminder_time),
                    value = uiState.prophylaxisDetails.reminderDateTime.toStringTime()
                )

                DetailRow(
                    label = stringResource(R.string.drug_name),
                    value = uiState.prophylaxisDetails.drugName
                )

                DetailRow(
                    label = stringResource(R.string.prophylaxis_dosage),
                    value = "${uiState.prophylaxisDetails.dosage} ${uiState.prophylaxisDetails.dosageUnit}"
                )

                if (originalResponseDateTime != 0L) {
                    DetailRow(
                        label = stringResource(R.string.prophylaxis_response_time),
                        value = originalResponseDateTime.toStringTime()
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = stringResource(R.string.infusion_performed),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClick(uiState.prophylaxisDetails.copy(responded = 1))
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = uiState.prophylaxisDetails.responded == 1,
                            onClick = {
                                onItemClick(
                                    uiState.prophylaxisDetails.copy(
                                        responded = 1,
                                        responseDateTime = Instant.now()
                                            .atZone(ZoneId.systemDefault()).toInstant()
                                            .toEpochMilli()
                                    )
                                )
                            }
                        )
                        Text(
                            text = stringResource(R.string.yes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClick(uiState.prophylaxisDetails.copy(responded = 0))
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = uiState.prophylaxisDetails.responded == 0,
                            onClick = {
                                onItemClick(uiState.prophylaxisDetails.copy(responded = 0))
                            }
                        )
                        Text(
                            text = stringResource(R.string.no),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        if (uiState.prophylaxisDetails.responded == 1) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Text(
                        text = stringResource(R.string.infusion_details),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
                    )

                    // Data e ora dell'infusione
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(text = uiState.prophylaxisDetails.date.toStringDate())
                            Spacer(modifier = Modifier.padding(1.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.padding(2.dp))

                        OutlinedButton(
                            onClick = { showTimePickerDialog = true },
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uiState.prophylaxisDetails.responseDateTime == 0L) Instant.now()
                                    .truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime()
                                else uiState.prophylaxisDetails.responseDateTime.toStringTime()
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_access_time_24),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = 8.dp)
                .width(150.dp),
            onClick = onSave,
            shape = MaterialTheme.shapes.small,
            enabled = uiState.prophylaxisDetails.responded != -1
        ) {
            Text(text = stringResource(id = R.string.save))
        }

        if (showDatePickerDialog) {
            ProphylaxisDatePickerDialog(
                initialDate = Instant.ofEpochMilli(uiState.prophylaxisDetails.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                onDateSelected = { localDate ->
                    val timestamp = localDate.atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()
                    onItemClick(uiState.prophylaxisDetails.copy(date = timestamp))
                },
                onDismiss = { showDatePickerDialog = false }
            )
        }

        if (showTimePickerDialog) {
            ProphylaxisTimePickerDialog(
                context = context,
                initialTime = LocalTime.parse(
                    if (uiState.prophylaxisDetails.responseDateTime == 0L) Instant.now()
                        .truncatedTo(ChronoUnit.MILLIS).toEpochMilli().toStringTime()
                    else uiState.prophylaxisDetails.responseDateTime.toStringTime(),
                    DateTimeFormatter.ofPattern("HH:mm")
                ),
                onTimeSelected = { timeString ->
                    val currentDate = Instant.ofEpochMilli(uiState.prophylaxisDetails.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val newTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                    val newDateTime = currentDate.atTime(newTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    onItemClick(uiState.prophylaxisDetails.copy(responseDateTime = newDateTime))
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProphylaxisDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

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
            datePicker.maxDate = today.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            setOnDismissListener { onDismiss() }
            setOnCancelListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose { datePickerDialog.dismiss() }
    }
}

@Composable
fun ProphylaxisTimePickerDialog(
    context: android.content.Context,
    initialTime: LocalTime,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                val formattedTime =
                    "${if (hour < 10) "0$hour" else hour}:${if (minute < 10) "0$minute" else minute}"
                onTimeSelected(formattedTime)
            },
            initialTime.hour,
            initialTime.minute,
            true
        ).apply {
            setOnDismissListener { onDismiss() }
            setOnCancelListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose { timePickerDialog.dismiss() }
    }
}