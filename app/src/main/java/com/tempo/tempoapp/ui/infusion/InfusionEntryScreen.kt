package com.tempo.tempoapp.ui.infusion

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.data.model.bleedingSite
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.bleeding.DatePickerDialog
import com.tempo.tempoapp.ui.bleeding.TextWithIcon
import com.tempo.tempoapp.ui.bleeding.TimePickerDialog
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch

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
    viewModel: InfusionEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        TempoAppBar(
            title = stringResource(id = InfusionEntryDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }) {
        InfusionEventBody(
            uiState = viewModel.uiState,
            onItemClick = viewModel::updateUiState,
            onSave = {
                coroutineScope.launch {
                    viewModel.onSave()
                    onNavigateUp()
                }
            },
            Modifier
                .padding(it)
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
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        InfusionEventInputForm(
            uiState,
            onItemClick,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = dimensionResource(id = R.dimen.padding_small))
                .width(150.dp),
            onClick = onSave,
            enabled = uiState.isEntryValid
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
    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }

    var showTimePickerDialog by remember {
        mutableStateOf(false)
    }
    var date by remember {
        mutableLongStateOf(uiState.infusionDetails.date)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {

        DropdownList(
            infusionDetails = uiState.infusionDetails,
            itemList = listOf("Profilassi", "On Demand", "Altro"),
            onItemClick = onItemClick,
            label = R.string.treatment
        )
        DropdownList(
            infusionDetails = uiState.infusionDetails,
            itemList = bleedingSite.toList(),
            onItemClick = onItemClick,
            label = R.string.infusion_site
        )
        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.dose_units)) },
            value = uiState.infusionDetails.doseUnits, onValueChange = {
                onItemClick(uiState.infusionDetails.copy(doseUnits = it))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        )
        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.lot_number)) },
            value = uiState.infusionDetails.lotNumber,
            onValueChange = {
                onItemClick(uiState.infusionDetails.copy(lotNumber = it))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small)),
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
                    text = uiState.infusionDetails.time,
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
                            uiState.infusionDetails.copy(
                                date = timestamp
                            )
                        )
                        date = timestamp
                    },
                    onDismiss = { showDatePickerDialog = !showDatePickerDialog })


            if (showTimePickerDialog)
                TimePickerDialog(
                    onTimeSelected = {
                        onItemClick(
                            uiState.infusionDetails.copy(time = it)
                        )
                    },
                    onDismiss = { showTimePickerDialog = !showTimePickerDialog })
        }

        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.note)) },
            value = uiState.infusionDetails.note ?: "",
            onValueChange = { onItemClick(uiState.infusionDetails.copy(note = it)) },
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
 * Composable function for displaying a dropdown list.
 *
 * @param infusionDetails Current details of the infusion event.
 * @param itemList List of items for the dropdown.
 * @param onItemClick Callback function for item click.
 * @param label Label resource ID for the dropdown.
 */
@Composable
fun DropdownList(
    infusionDetails: InfusionDetails,
    itemList: List<String>,
    modifier: Modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
    onItemClick: (InfusionDetails) -> Unit,
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
            when (label) {
                R.string.infusion_site -> infusionDetails.infusionSite.ifBlank {
                    stringResource(
                        id = label
                    )
                }

                R.string.treatment -> infusionDetails.treatment.ifBlank {
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
                        when (label) {
                            R.string.infusion_site -> {
                                onItemClick(
                                    infusionDetails.copy(infusionSite = it)
                                )
                            }

                            R.string.treatment -> {
                                onItemClick(
                                    infusionDetails.copy(
                                        treatment = it
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
