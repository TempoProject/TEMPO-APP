package com.tempo.tempoapp.ui.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.HomeBody
import com.tempo.tempoapp.ui.bleeding.DatePickerDialog
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navigateToBleedingUpdate: (Int) -> Unit,
    navigateToInfusionUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateUp: () -> Unit,
) {


    val historyUiState by viewModel.historyUiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }
    var date by remember {
        mutableStateOf(Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli().toStringDate())
    }

    Scaffold(topBar = {
        TempoAppBar(
            title = date,
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDatePickerDialog = !showDatePickerDialog
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null
                )
            }
        }) { innerPadding ->
        HomeBody(
            bleedingEventList = historyUiState.bleedingList,
            infusionEventList = historyUiState.infusionList,
            stepsCount = historyUiState.stepsCount,
            onInfusionItemClick = navigateToInfusionUpdate,
            onBleedingItemClick = navigateToBleedingUpdate,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )

        if (showDatePickerDialog)
            DatePickerDialog(
                onDateSelected = { timestamp ->
                    scope.launch {
                        viewModel.updateSteps(timestamp)
                    }
                    scope.launch {
                        viewModel.updateBleeding(timestamp)
                    }
                    scope.launch {
                        viewModel.updateInfusion(timestamp)
                    }
                    date = timestamp.toStringDate()
                },
                onDismiss = { showDatePickerDialog = !showDatePickerDialog })
    }
}
