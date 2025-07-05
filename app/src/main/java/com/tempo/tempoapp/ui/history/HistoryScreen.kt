package com.tempo.tempoapp.ui.history

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.HomeBody
import com.tempo.tempoapp.ui.bleeding.BleedingEventDetailsDestination
import com.tempo.tempoapp.ui.infusion.InfusionEntryDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Represents a destination for the history screen in the navigation system.
 * Implements the [NavigationDestination] interface.
 */
object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history
}

private const val TAG = "HistoryScreen"

/**
 * Represents a screen for displaying the history of bleeding and infusion events.
 *
 * @param navigateToBleedingUpdate Callback to navigate to the update screen for a bleeding event.
 * @param navigateToInfusionUpdate Callback to navigate to the update screen for an infusion event.
 * @param modifier Modifier for the screen.
 * @param viewModel View model for managing the state and logic of the history screen.
 * @param onNavigateUp Callback to handle navigation up action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    onNavigateUp: () -> Unit,
) {

    val viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val historyUiState by viewModel.historyUiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TempoAppBar(
                title = selectedDate.toStringDate(),
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
            combinedEvent = historyUiState.combinedEvents,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            onInfusionItemClick = { id ->
                Log.d(TAG, "Infusion item clicked: $id")
                navController?.navigate("${InfusionEntryDestination.route}/${id}")
            },
            onBleedingItemClick = { id ->
                Log.d(TAG, "Bleeding item clicked: $id")
                navController?.navigate("${BleedingEventDetailsDestination.route}/${id}")
            },
            onProphylaxisItemClick = {
                // TODO
            }
        )

        if (showDatePickerDialog) {
            HistoryDatePickerDialog(
                initialDate = Instant.ofEpochMilli(selectedDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(),
                onDateSelected = { localDate ->
                    Log.d(TAG, "Selected date timestamp: $localDate")
                    viewModel.updateSelectedDate(localDate)
                    showDatePickerDialog = false
                },
                onDismiss = { showDatePickerDialog = false }
            )
        }
    }
}

@Composable
fun HistoryDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
            datePicker.maxDate = System.currentTimeMillis()
            setOnDismissListener { onDismiss() }
            setOnCancelListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose { datePickerDialog.dismiss() }
    }
}