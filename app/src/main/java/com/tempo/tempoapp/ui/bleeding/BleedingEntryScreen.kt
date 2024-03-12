package com.tempo.tempoapp.ui.bleeding

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.BleedingCause
import com.tempo.tempoapp.data.model.Severity
import com.tempo.tempoapp.data.model.bleedingSite
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

object BleedingEntryDestination : NavigationDestination {
    override val route: String
        get() = "bleedingEvent"
    override val titleRes: Int
        get() = R.string.add_new_bleeding
}

@Composable
fun BleedingEntryScreen(viewModel: BleedingEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    // TODO aggiungere topBar
    Scaffold { innerPadding ->
        val coroutineScope = rememberCoroutineScope()
        BleedingEventBody(
            uiState = viewModel.uiState,
            onItemClick = viewModel::updateUiState,
            onSave = {
                coroutineScope.launch {
                    viewModel.onSave()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
        )

    }
}

@Composable
fun BleedingEventBody(
    uiState: BleedingEventUiState,
    onItemClick: (BleedingDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier.padding(8.dp)
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        BleedingEventInputForm(
            uiState,
            onItemClick,
            modifier.padding(8.dp)
        )
        // TODO check che tutti i valori sono corretti
        Button(
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = 8.dp)
                .width(150.dp),
            onClick = onSave,
            enabled = uiState.isEntryValid
        ) {
            Text(text = "Salva")
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleedingEventInputForm(
    uiState: BleedingEventUiState,
    onItemClick: (BleedingDetails) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }

    var showTimePickerDialog by remember {
        mutableStateOf(false)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.fillMaxWidth()) {
        DropdownList(
            uiState.bleedingDetails,
            itemList = bleedingSite.toList(),
            onItemClick = onItemClick,
            label = R.string.site_string_label,
            modifier = modifier
        )

        DropdownList(
            uiState.bleedingDetails,
            itemList = BleedingCause.entries.map {
                it.name
            },
            onItemClick = onItemClick,
            label = R.string.cause_string_label,
            modifier = modifier
        )

        DropdownList(
            uiState.bleedingDetails,
            itemList = Severity.entries.map { it.name },
            onItemClick = onItemClick,
            label = R.string.pain_scale_string_label,
            modifier = modifier
        )

        DropdownList(
            uiState.bleedingDetails,
            itemList = Severity.entries.map { it.name },
            onItemClick = onItemClick,
            label = R.string.severity_string_label,
            modifier = modifier
        )

        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            var date by remember {
                mutableStateOf(uiState.bleedingDetails.date)
            }
            OutlinedButton(
                onClick = { showDatePickerDialog = !showDatePickerDialog },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(2f)
            ) {
                Text(text = date)
                Spacer(modifier = Modifier.padding(1.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                    contentDescription = null
                )
            }
            /*
            OutlinedTextField(
                label = { Text(text = "Data") },
                value = uiState.bleedingDetails.date,
                onValueChange = {},
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),


            )*/
            Spacer(modifier = Modifier.padding(2.dp))
            OutlinedButton(
                onClick = {
                    showTimePickerDialog = !showTimePickerDialog
                }, shape = RoundedCornerShape(8.dp), modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = uiState.bleedingDetails.time,
                )
                Icon(
                    painter = painterResource(id = R.drawable.baseline_access_time_24),
                    contentDescription = null
                )
            }

            if (showDatePickerDialog)
                DatePickerDialog(
                    onDateSelected = { timestamp ->
                        onItemClick(
                            uiState.bleedingDetails.copy(
                                date = SimpleDateFormat("dd-MM-yyyy").format(
                                    Date(timestamp)
                                )
                            )
                        )
                        date = SimpleDateFormat("dd-MM-yyyy").format(
                            Date(timestamp)
                        )
                    },
                    onDismiss = { showDatePickerDialog = !showDatePickerDialog })

            if (showTimePickerDialog)
                TimePickerDialog(
                    onTimeSelected = {
                        onItemClick(
                            uiState.bleedingDetails.copy(time = it)
                        )
                    },
                    onDismiss = { showTimePickerDialog = !showTimePickerDialog })
            /*
            OutlinedTextField(
                label = { Text(text = "Ora") },
                value = uiState.bleedingDetails.time,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                keyboardActions = KeyboardActions(),
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
            )*/
        }

        OutlinedTextField(
            label = { Text(text = "Note") },
            value = uiState.bleedingDetails.note,
            onValueChange = { onItemClick(uiState.bleedingDetails.copy(note = it)) },
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialog(onDateSelected: (Long) -> Unit, onDismiss: () -> Unit) {

    val state = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        Button(onClick = {
            state.selectedDateMillis?.let { onDateSelected(it) }
            onDismiss()
        }) {
            Text(text = "OK")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text(text = "Cancel")
        }
    }
    ) {
        DatePicker(
            state = state
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerDialog(onTimeSelected: (String) -> Unit, onDismiss: () -> Unit) {

    val state = rememberTimePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column {
                TimePicker(state = state)
                Row {
                    Button(onClick = onDismiss) {
                        Text(text = "Cancel")
                    }
                    Button(onClick = {
                        // TODO check AM/PM
                        onTimeSelected("${if(state.hour == 0) "00" else state.hour}:${if (state.minute == 0) "00" else state.minute}")
                        onDismiss()
                    }) {
                        Text(text = "Salva")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownList(
    bleedingDetails: BleedingDetails,
    itemList: List<String>,
    modifier: Modifier = Modifier,
    onItemClick: (BleedingDetails) -> Unit,
    @StringRes label: Int,
) {
    var showDropdown by remember {
        mutableStateOf(false)
    }

    var labelText by remember {
        mutableStateOf("")
    }

    Box(
        modifier = modifier
            .clickable { showDropdown = !showDropdown }
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
    ) {
        TextWithIcon(text = labelText.ifBlank {
            // check on the first field because if empty means that is a new event
            if (bleedingDetails.cause.isNotEmpty())
                when (label) {
                    R.string.cause_string_label -> bleedingDetails.cause
                    R.string.site_string_label -> bleedingDetails.site
                    R.string.severity_string_label -> bleedingDetails.severity
                    R.string.pain_scale_string_label -> bleedingDetails.painScale
                    else -> ""
                }
            else
                stringResource(id = label)
        }, modifier)
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = !showDropdown },
            modifier,
        ) {
            itemList.forEach {
                DropdownMenuItem(
                    text = { Text(text = it, textAlign = TextAlign.Center) },
                    onClick = {
                        //println(it)
                        when (label) {
                            R.string.site_string_label -> {
                                onItemClick(
                                    bleedingDetails.copy(site = it)
                                )
                            }

                            R.string.cause_string_label -> onItemClick(bleedingDetails.copy(cause = it))
                            R.string.pain_scale_string_label -> onItemClick(
                                bleedingDetails.copy(
                                    painScale = it
                                )
                            )

                            else -> {
                                println(it)
                                onItemClick(
                                    bleedingDetails.copy(
                                        severity = it
                                    )
                                )
                            }
                        }
                        showDropdown = !showDropdown
                        labelText = it
                    },
                    contentPadding = PaddingValues(8.dp)
                )
            }
        }
    }
}

@Composable
fun TextWithIcon(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text)
        Spacer(modifier = Modifier.width(4.dp)) // Spazio tra l'icona e il testo
        Icon(
            painter = painterResource(id = R.drawable.arrow_downward), contentDescription = text,
        )
    }
}
