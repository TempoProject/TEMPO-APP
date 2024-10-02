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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch

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
    viewModel: BleedingEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
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
                    viewModel.onSave()
                    onNavigateUp()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        )

    }
}

/**
 * Composable function representing the body of the bleeding event screen.
 *
 * @param uiState UI state of the bleeding event.
 * @param onItemClick Callback function invoked when an item is clicked.
 * @param onSave Callback function invoked when the user saves the bleeding event.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun BleedingEventBody(
    uiState: BleedingEventUiState,
    onItemClick: (BleedingDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier//.padding(8.dp)
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        BleedingEventInputForm(
            uiState,
            onItemClick,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = 8.dp)
                .width(150.dp),
            onClick = onSave,
            enabled = uiState.isEntryValid,
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

    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }

    var showTimePickerDialog by remember {
        mutableStateOf(false)
    }
    var date by remember {
        mutableLongStateOf(uiState.bleedingDetails.date)
    }
    val radioOptions =
        listOf(stringResource(id = R.string.yes), stringResource(id = R.string.no))
    var isABleedingEpisode by remember { mutableStateOf(uiState.bleedingDetails.isABleedingEpisode) }
    var questionBleedingEpisode by remember { mutableStateOf(uiState.bleedingDetails.questionBleedingEpisode) }
    var questionTreatment by remember { mutableStateOf(uiState.bleedingDetails.treatment) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        DropdownList(
            uiState.bleedingDetails,
            itemList = stringArrayResource(id = R.array.site_array).toList(),//
            onItemClick = onItemClick,
            label = R.string.site_string_label
        )

        Text(
            text = stringResource(R.string.is_a_bleeding_event),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            )
            //modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            radioOptions.forEach { options ->
                println(isABleedingEpisode)
                Text(
                    text = options,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodySmall
                )
                RadioButton(
                    selected = (options == isABleedingEpisode),//uiState.bleedingDetails.isABleedingEpisode),
                    onClick = {
                        isABleedingEpisode = options
                        onItemClick(
                            uiState.bleedingDetails.copy(
                                isABleedingEpisode = options,
                                questionBleedingEpisode = radioOptions[1],
                            )
                        )

                    })
            }
        }
        if (isABleedingEpisode == stringResource(id = R.string.no)) {
            Text(
                text = stringResource(R.string.do_you_think_is_a_bleeding),
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                radioOptions.forEach { options ->
                    Text(
                        text = options,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                        style = MaterialTheme.typography.bodySmall
                    )
                    RadioButton(
                        selected = (options == questionBleedingEpisode),
                        onClick = {
                            questionBleedingEpisode = options
                            onItemClick(
                                uiState.bleedingDetails.copy(
                                    questionBleedingEpisode = options,
                                    isABleedingEpisode = radioOptions[1]
                                )
                            )
                        })
                }
            }
        }
        Text(
            text = stringResource(R.string.did_you_treat_yself),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            ),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            radioOptions.forEach { options ->
                Text(
                    text = options,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodySmall
                )
                RadioButton(
                    selected = (options == questionTreatment),
                    onClick = { questionTreatment = options })
            }
        }
        DropdownList(
            uiState.bleedingDetails,
            itemList = stringArrayResource(id = R.array.cause_array).toList(),
            onItemClick = onItemClick,
            label = R.string.cause_string_label,
        )

        var sliderPosition by remember { mutableFloatStateOf(uiState.bleedingDetails.painScale.toFloat()) }
        Text(
            text = stringResource(id = R.string.pain_scale_string_label) + ": " + sliderPosition.toInt(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            )
            //modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
        )

        Slider(
            value = sliderPosition,
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_medium),
                end = dimensionResource(id = R.dimen.padding_medium)
            ),
            steps = 9,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..10f,
            onValueChangeFinished = {
                onItemClick(
                    uiState.bleedingDetails.copy(
                        painScale = sliderPosition.toString()
                    )
                )
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            OutlinedButton(
                onClick = { showDatePickerDialog = !showDatePickerDialog },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier
                    .weight(2f)
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
                onClick = {
                    showTimePickerDialog = !showTimePickerDialog
                },
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
                modifier = Modifier
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
                        println("date: $timestamp")
                        onItemClick(
                            uiState.bleedingDetails.copy(
                                date = timestamp//timestamp.toStringDate()
                            )
                        )
                        date = timestamp//timestamp.toStringDate()
                    },
                    onDismiss = { showDatePickerDialog = !showDatePickerDialog })

            if (showTimePickerDialog)
                TimePickerDialog(
                    onTimeSelected = {
                        println("time $it")
                        onItemClick(
                            uiState.bleedingDetails.copy(time = it)
                        )
                    },
                    onDismiss = { showTimePickerDialog = !showTimePickerDialog })
        }

        OutlinedTextField(
            label = { Text(text = "Note") },
            value = uiState.bleedingDetails.note ?: "",
            onValueChange = { onItemClick(uiState.bleedingDetails.copy(note = it)) },
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
        )
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

/**
 * Composable function for rendering a dropdown list.
 *
 * @param bleedingDetails Details of the bleeding event.
 * @param itemList List of items to display in the dropdown.
 * @param modifier Modifier for customizing the layout.
 * @param onItemClick Callback function invoked when an item is clicked.
 * @param label Resource ID for the label associated with the dropdown.
 */
@Composable
fun DropdownList(
    bleedingDetails: BleedingDetails,
    itemList: List<String>,
    modifier: Modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
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
            .border(
                1.dp,
                Color.Black,
                RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))
            )
    ) {
        TextWithIcon(text = labelText.ifBlank {
            // check on the first field because if empty means that is a new event
            when (label) {
                R.string.cause_string_label -> bleedingDetails.cause.ifBlank {
                    stringResource(id = label)
                }

                R.string.site_string_label -> bleedingDetails.site.ifBlank {
                    stringResource(id = label)
                }

                else -> stringResource(id = label)
            }
        }, modifier)
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = !showDropdown },
            offset = DpOffset(
                x = dimensionResource(id = R.dimen.padding_small),
                y = dimensionResource(id = R.dimen.padding_small)
            )
        ) {
            itemList.forEach {
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(text = it, textAlign = TextAlign.Center) },
                    onClick = {
                        //println(it)
                        when (label) {
                            R.string.site_string_label -> {
                                onItemClick(
                                    bleedingDetails.copy(site = it)
                                )
                            }

                            R.string.cause_string_label -> onItemClick(
                                bleedingDetails.copy(
                                    cause = it
                                )
                            )

                            else -> {
                                println(it)
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

/**
 * Composable function for rendering text with an icon.
 *
 * @param text Text to display.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun TextWithIcon(text: String, modifier: Modifier = Modifier) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(4.dp)) // Spazio tra l'icona e il testo
        Icon(
            painter = painterResource(id = R.drawable.arrow_downward), contentDescription = text,
        )
    }
}
