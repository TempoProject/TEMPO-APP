package com.tempo.tempoapp.ui.infusion

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.DosageUnit
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Navigation destination for the infusion entry screen.
 */
object InfusionEntryDestination : NavigationDestination {
    override val route: String
        get() = "infusion_entry"
    override val titleRes: Int
        get() = R.string.add_infusion
}

/**
 * Composable function for displaying the infusion entry screen.
 *
 * @param onNavigateUp Function to handle the up navigation.
 * @param viewModel ViewModel for managing the state of the infusion entry screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionEventScreen(
    onNavigateUp: () -> Unit,
) {
    val viewModel: InfusionEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = InfusionEntryDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        InfusionEventBody(
            uiState = viewModel.uiState,
            onItemClick = viewModel::updateUiState,
            onSave = {
                coroutineScope.launch {
                    val success = viewModel.onSave()
                    if (success) {
                        onNavigateUp()
                    }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        )
    }
}

/**
 * Composable function representing the body of the infusion event screen.
 *
 * @param uiState Current UI state of the infusion event.
 * @param onItemClick Callback function for item click.
 * @param onSave Callback function for saving the infusion event.
 * @param modifier Modifier for the body.
 */
@Composable
fun InfusionEventBody(
    uiState: InfusionUiState,
    onItemClick: (InfusionDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        InfusionEventInputForm(
            uiState = uiState,
            onItemClick = onItemClick,
            modifier = Modifier.fillMaxWidth()
        )

        // Mostra errori solo se l'utente ha tentato di salvare
        if (uiState.shouldShowErrors()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Text(
                        text = "Correggere i seguenti errori:",
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
 * Composable function for displaying the input form of the infusion event.
 *
 * @param uiState Current UI state of the infusion event.
 * @param onItemClick Callback function for item click.
 * @param modifier Modifier for the input form.
 */
@Composable
fun InfusionEventInputForm(
    uiState: InfusionUiState,
    onItemClick: (InfusionDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var date by remember { mutableLongStateOf(uiState.infusionDetails.date) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {

        // Motivo (obbligatorio)
        InfusionDropdownWithError(
            value = uiState.infusionDetails.reason,
            itemList = listOf(
                "Trauma",
                "Procedure/Chirurgia",
                "Previsione attività fisica",
                "Recupero regolare profilassi"
            ),
            onItemSelected = { onItemClick(uiState.infusionDetails.copy(reason = it)) },
            label = "Motivo *",
            hasError = uiState.hasError("reason"),
            errorMessage = stringResource(uiState.getError("reason") ?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth()
        )

        // Nome del farmaco (obbligatorio)
        OutlinedTextFieldWithError(
            value = uiState.infusionDetails.drugName,
            onValueChange = { onItemClick(uiState.infusionDetails.copy(drugName = it)) },
            label = "Nome del farmaco *",
            hasError = uiState.hasError("drugName"),
            errorMessage = stringResource(uiState.getError("drugName")?: R.string.dummy_value),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dose (obbligatorio)
            OutlinedTextFieldWithError(
                value = uiState.infusionDetails.dose,
                onValueChange = { onItemClick(uiState.infusionDetails.copy(dose = it)) },
                label = "Dose *",
                hasError = uiState.hasError("dose"),
                errorMessage = if (uiState.hasError("dose")) stringResource(uiState.getError("dose") ?: R.string.dummy_value) else null,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            InfusionDosageUnitDropdown(
                selectedUnit = uiState.infusionDetails.dosageUnit,
                onSelect = { unit ->
                    onItemClick(uiState.infusionDetails.copy(dosageUnit = unit))
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Batch number (facoltativo)
        OutlinedTextField(
            value = uiState.infusionDetails.batchNumber,
            onValueChange = { onItemClick(uiState.infusionDetails.copy(batchNumber = it)) },
            label = { Text("Batch number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // Data e ora (obbligatori)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { showDatePickerDialog = true },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier.weight(2f)
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
                Text(text = uiState.infusionDetails.time)
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

        // Note (facoltativo)
        OutlinedTextField(
            value = uiState.infusionDetails.note ?: "",
            onValueChange = { onItemClick(uiState.infusionDetails.copy(note = it)) },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            minLines = 3
        )

        if (showDatePickerDialog) {
            InfusionDatePickerDialog(
                initialDate = Instant.ofEpochMilli(date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                onDateSelected = { localDate ->
                    val timestamp = localDate.atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()
                    onItemClick(uiState.infusionDetails.copy(date = timestamp))
                    date = timestamp
                },
                onDismiss = { showDatePickerDialog = false }
            )
        }

        if (showTimePickerDialog) {
            InfusionTimePickerDialog(
                context = LocalContext.current,
                initialTime = LocalTime.parse(
                    uiState.infusionDetails.time.ifBlank { "12:00" },
                    DateTimeFormatter.ofPattern("H:mm")
                ),
                onTimeSelected = { time ->
                    onItemClick(uiState.infusionDetails.copy(time = time))
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionDropdownWithError(
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

@Composable
fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hasError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
            keyboardOptions = keyboardOptions,
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
fun InfusionDatePickerDialog(
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
            // Imposta la data massima ad oggi
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
fun InfusionTimePickerDialog(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                val formattedTime = "${if (hour < 10) "0$hour" else hour}:${if (minute < 10) "0$minute" else minute}"
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

@Composable
fun InfusionDosageUnitDropdown(
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