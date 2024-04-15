package com.tempo.tempoapp.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.data.model.ReminderEvent
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import kotlinx.coroutines.launch


object ReminderListDestination : NavigationDestination {
    override val route: String
        get() = "reminderList"
    override val titleRes: Int
        get() = R.string.reminder
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderList(
    onNavigateUp: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: ReminderListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.reminderListUiState.collectAsState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = ReminderListDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) {
        ReminderListBody(
            uiState.value.reminderList,
            deleteReminder = {
                scope.launch {
                    viewModel.deleteReminder(it)
                }
            },
            Modifier.padding(it)
        )
    }
}

@Composable
private fun ReminderListBody(
    reminderList: List<ReminderEvent>,
    deleteReminder: (ReminderEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(reminderList) { item ->
            ReminderItem(
                item, deleteReminder, Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
private fun ReminderItem(
    item: ReminderEvent,
    deleteReminder: (ReminderEvent) -> Unit,
    modifier: Modifier
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
/*            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            )*/
            Text(
                text = item.event,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            if (item.isPeriodic)
                Text(text = "Il promemoria si ripete ogni: ${item.period} ${item.timeUnit}")
            /*Text(
                text = "Periodico? ${if (item.isPeriodic) " Si " else " No"}",
                style = MaterialTheme.typography.titleMedium
            )*/
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.timestamp.toStringDate()}, ${item.timestamp.toStringTime()}",
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { deleteReminder(item) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }


        }
    }

}
