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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

object InfusionEntryDestination : NavigationDestination {
    override val route: String
        get() = "infusion_entry"
    override val titleRes: Int
        get() = R.string.infusion

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionEventScreen(
    navigateBack: () -> Unit,
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
                    navigateBack()
                }
            },
            Modifier.padding(it)
        )
    }
}

@Composable
fun InfusionEventBody(
    uiState: InfusionUiState,
    onItemClick: (InfusionDetails) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        InfusionEventInputForm(
            uiState,
            onItemClick,
            modifier.padding(8.dp)
        )
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
        mutableStateOf(uiState.infusionDetails.date)
    }

    //println(uiState.infusionDetails)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.fillMaxWidth()) {

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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.lot_number)) },
            value = uiState.infusionDetails.lotNumber,
            onValueChange = {
                onItemClick(uiState.infusionDetails.copy(lotNumber = it))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            Spacer(modifier = Modifier.padding(2.dp))

            OutlinedButton(
                onClick = {
                    showTimePickerDialog = !showTimePickerDialog
                }, shape = RoundedCornerShape(8.dp), modifier = Modifier
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
                            uiState.infusionDetails.copy(time = it)
                        )
                    },
                    onDismiss = { showTimePickerDialog = !showTimePickerDialog })
        }

        OutlinedTextField(
            label = { Text(text = stringResource(id = R.string.note)) },
            value = uiState.infusionDetails.note ?: "",
            onValueChange = { onItemClick(uiState.infusionDetails.copy(note = it)) },
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DropdownList(
    infusionDetails: InfusionDetails,
    itemList: List<String>,
    modifier: Modifier = Modifier,
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
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
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
            modifier,
        ) {
            itemList.forEach {
                DropdownMenuItem(
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
