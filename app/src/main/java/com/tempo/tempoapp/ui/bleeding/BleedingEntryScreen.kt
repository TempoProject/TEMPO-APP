package com.tempo.tempoapp.ui.bleeding

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.filterDoseInput
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Represents the destination for navigating to the bleeding event screen.
 * Implements the NavigationDestination interface to define navigation route and title resource.
 */
object BleedingEntryDestination : NavigationDestination {
    override val route: String
        get() = "bleeding_event"
    override val titleRes: Int
        get() = R.string.add_event
}

/**
 * Composable function for rendering the bleeding event screen.
 *
 * @param onNavigateUp Callback function to handle the up navigation action.
 * @param viewModel ViewModel responsible for managing the state and logic of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleedingEntryScreen(
    onNavigateUp: () -> Unit,
) {
    val viewModel: BleedingEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = BleedingEntryDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
    ) { innerPadding ->
        BleedingEventBody(
            uiState = viewModel.uiState,
            onItemClick = viewModel::updateUiState,
            onSave = {
                coroutineScope.launch {
                    val success = viewModel.onSave()
                    if (success)
                        onNavigateUp()

                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        )
    }
}

@Composable
fun BleedingEventBody(
    uiState: BleedingEventUiState,
    onItemClick: (BleedingDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        BleedingEventInputForm(
            uiState = uiState,
            onItemClick = onItemClick,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.shouldShowErrors()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Text(
                        text = stringResource(R.string.error_validation_header),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    uiState.validationErrors.values.forEach { error ->
                        Text(
                            text = stringResource(error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
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
        ) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

/**
 * Composable function for rendering the form inputs of the bleeding event.
 *
 * @param uiState UI state of the bleeding event.
 * @param onItemClick Callback function invoked when an item is clicked.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun BleedingEventInputForm(
    uiState: BleedingEventUiState,
    onItemClick: (BleedingDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var date by remember { mutableLongStateOf(uiState.bleedingDetails.date) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {


        DropdownListWithError(
            value = uiState.bleedingDetails.eventType,
            itemList = stringArrayResource(R.array.type_of_events).asList(),
            onItemSelected = { onItemClick(uiState.bleedingDetails.copy(eventType = it)) },
            label = stringResource(R.string.event),
            hasError = uiState.hasError("eventType"),
            errorMessage = stringResource(uiState.getError("eventType") ?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth()
        )

        // Causa dell'evento (obbligatorio)
        DropdownListWithError(
            value = uiState.bleedingDetails.cause,
            itemList = stringArrayResource(R.array.cause_array).asList(),
            onItemSelected = { onItemClick(uiState.bleedingDetails.copy(cause = it)) },
            label = stringResource(R.string.cause_string_label),
            hasError = uiState.hasError("cause"),
            errorMessage = stringResource(uiState.getError("cause") ?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth()
        )

        // Location site (obbligatorio)
        DropdownListWithError(
            value = uiState.bleedingDetails.site,
            itemList = stringArrayResource(id = R.array.site_array).toList(),
            onItemSelected = { onItemClick(uiState.bleedingDetails.copy(site = it)) },
            label = stringResource(R.string.site_string_label),
            hasError = uiState.hasError("site"),
            errorMessage = stringResource(uiState.getError("site") ?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth()
        )

        // Did you treat yourself (obbligatorio)
        RadioButtonGroupWithError(
            selectedValue = uiState.bleedingDetails.treatment,
            options = listOf(stringResource(R.string.yes), stringResource(R.string.no)),
            onSelectionChanged = { onItemClick(uiState.bleedingDetails.copy(treatment = it)) },
            label = stringResource(R.string.did_you_treat_yself),
            hasError = uiState.hasError("treatment"),
            errorMessage = stringResource(uiState.getError("treatment") ?: R.string.dummy_value),
        )

        if (uiState.bleedingDetails.treatment == stringResource(R.string.yes)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Tipo di farmaco (obbligatorio)
                    OutlinedTextFieldWithError(
                        value = uiState.bleedingDetails.medicationType,
                        onValueChange = { onItemClick(uiState.bleedingDetails.copy(medicationType = it)) },
                        label = stringResource(R.string.drug_name),
                        hasError = uiState.hasError("medicationType"),
                        keyboardType = KeyboardType.Text,
                        errorMessage = stringResource(
                            uiState.getError("medicationType") ?: R.string.dummy_value
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextFieldWithError(
                            value = uiState.bleedingDetails.dose,
                            onValueChange = {
                                onItemClick(
                                    uiState.bleedingDetails.copy(
                                        dose = filterDoseInput(
                                            it
                                        )
                                    )
                                )
                            },
                            label = stringResource(R.string.dose_units),
                            hasError = uiState.hasError("dose"),
                            keyboardType = KeyboardType.Number,
                            errorMessage = stringResource(
                                uiState.getError("dose") ?: R.string.dummy_value
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        BleedingDosageUnitDropdown(
                            selectedUnit = uiState.bleedingDetails.dosageUnit,
                            onSelect = { unit ->
                                onItemClick(uiState.bleedingDetails.copy(dosageUnit = unit))
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Lot Number (facoltativo)
                    OutlinedTextField(
                        value = uiState.bleedingDetails.lotNumber,
                        onValueChange = { onItemClick(uiState.bleedingDetails.copy(lotNumber = it.filter { it.isDigit() })) },
                        label = { Text(stringResource(R.string.batch_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))
                    )
                }
            }
        }

        // Pain Scale (obbligatorio)
        var sliderPosition by remember {
            mutableFloatStateOf(
                uiState.bleedingDetails.painScale.toFloatOrNull() ?: 0f
            )
        }

        Column {
            Text(
                text = stringResource(R.string.pain_scale_string_label) + " ${sliderPosition.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (uiState.hasError("painScale")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            Slider(
                value = sliderPosition,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
                steps = 9,
                onValueChange = { sliderPosition = it },
                valueRange = 0f..10f,
                onValueChangeFinished = {
                    onItemClick(
                        uiState.bleedingDetails.copy(
                            painScale = sliderPosition.toInt().toString()
                        )
                    )
                },
                colors = if (uiState.hasError("painScale")) {
                    SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.error)
                } else SliderDefaults.colors()
            )

            if (uiState.hasError("painScale")) {
                Text(
                    text = stringResource(uiState.getError("painScale") ?: R.string.dummy_value),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
                )
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { showDatePickerDialog = true },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier.weight(2f),
                colors = if (false) {
                    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(text = date.toStringDate())
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
                modifier = Modifier.weight(1f),
                colors = if (uiState.hasError("time")) {
                    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(text = uiState.bleedingDetails.time)
                Icon(
                    painter = painterResource(id = R.drawable.baseline_access_time_24),
                    contentDescription = null
                )
            }
        }

        if (uiState.hasError("time")) {
            Text(
                text = stringResource(uiState.getError("time") ?: R.string.dummy_value),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }


        val isNoteRequired = uiState.bleedingDetails.site == "Other"
        OutlinedTextFieldWithError(
            value = uiState.bleedingDetails.note ?: "",
            onValueChange = { onItemClick(uiState.bleedingDetails.copy(note = it)) },
            label = if (isNoteRequired) stringResource(R.string.event_location_placeholder) else stringResource(
                R.string.note
            ),
            hasError = uiState.hasError("note"),
            errorMessage = stringResource(uiState.getError("note") ?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Text,
            minLines = 3
        )

        if (showDatePickerDialog) {
            BleedingDatePickerDialog(
                initialDate = Instant.ofEpochMilli(date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                onDateSelected = { localDate ->
                    val timestamp = localDate.atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()
                    onItemClick(uiState.bleedingDetails.copy(date = timestamp))
                    date = timestamp
                },
                onDismiss = { showDatePickerDialog = false }
            )
        }

        if (showTimePickerDialog) {
            BleedingTimePickerDialog(
                context = LocalContext.current,
                initialTime = LocalTime.now(),
                onTimeSelected = { time ->
                    onItemClick(uiState.bleedingDetails.copy(time = time))
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }
    }
}

@Composable
fun BleedingDatePickerDialog(
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
fun BleedingTimePickerDialog(
    context: Context,
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
            true // 24 hour format
        ).apply {
            // Aggiungi i listener per dismiss/cancel
            setOnDismissListener { onDismiss() }
            setOnCancelListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose { timePickerDialog.dismiss() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownListWithError(
    value: String,
    itemList: List<String>,
    onItemSelected: (String) -> Unit,
    label: String,
    hasError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { }, // Read-only
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = if (hasError) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.error,
                        unfocusedLabelColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                isError = hasError
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                itemList.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        if (hasError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.padding_medium),
                    top = 4.dp
                )
            )
        }
    }
}


/**
 * Composable function for rendering the date picker dialog.
 *
 * @param onDateSelected Callback function invoked when a date is selected.
 * @param onDismiss Callback function invoked when the dialog is dismissed.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {

    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                state.selectedDateMillis?.let { onDateSelected(it) }
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = state
        )
    }
}

/**
 * Composable function for rendering the time picker dialog.
 *
 * @param onTimeSelected Callback function invoked when a time is selected.
 * @param onDismiss Callback function invoked when the dialog is dismissed.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = rememberTimePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Card {
            TimePicker(
                state = state,
                modifier = Modifier
                    .padding(
                        dimensionResource(id = R.dimen.padding_large)
                    )
                    .fillMaxWidth()
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
                Button(onClick = {
                    onTimeSelected("${if (state.hour == 0) "00" else state.hour}:${if (state.minute == 0) "00" else state.minute}")
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}

@Composable
fun RadioButtonGroupWithError(
    selectedValue: String,
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    label: String,
    hasError: Boolean,
    errorMessage: String?
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            options.forEach { option ->
                Text(
                    text = option,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodySmall
                )
                RadioButton(
                    selected = option == selectedValue,
                    onClick = { onSelectionChanged(option) },
                    colors = if (hasError) {
                        RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                    } else RadioButtonDefaults.colors()
                )
            }
        }

        if (hasError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hasError: Boolean,
    errorMessage: String?,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            isError = hasError,
            minLines = minLines,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            colors = if (hasError) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = MaterialTheme.colorScheme.error
                )
            } else OutlinedTextFieldDefaults.colors()
        )

        if (hasError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
fun BleedingDosageUnitDropdown(
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
fun TextWithIcon(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
            color = textColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.arrow_downward),
            contentDescription = text,
            tint = textColor
        )
    }
}