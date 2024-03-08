package com.tempo.tempoapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination


object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToBleedingEntry: () -> Unit
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToBleedingEntry,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null//stringResource(R.string.item_entry_title)
                )
            }
        },
    ) { innerPadding ->

        HomeBody(
            homeUiState.itemList, modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )

    }
}

@Composable
fun HomeBody(itemList: List<BleedingEvent>, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (itemList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_bleeding_event),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            BleedingList(itemList, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun BleedingList(itemList: List<BleedingEvent>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(itemList) {
            BleedingItem(item = it, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun BleedingItem(item: BleedingEvent, modifier: Modifier) {
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.bleedingCause,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = item.bleedingSite,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
