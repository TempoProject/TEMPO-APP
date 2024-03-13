package com.tempo.tempoapp.ui.infusion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.bleeding.ItemDetailsRow
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object InfusionDetailsDestination : NavigationDestination {
    override val route: String
        get() = "infusion_details"
    override val titleRes: Int
        get() = R.string.details

    const val itemIdArg = "itemId"
    val routeWithArgs = "${InfusionEntryDestination.route}/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionDetailsScreen(
    onNavigateUp: () -> Unit,
    navigateBack: () -> Unit,
    navigateToInfusionEdit: (Int) -> Unit,
    viewModel: InfusionDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = InfusionDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { navigateToInfusionEdit(uiState.value.id) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null//stringResource(R.string.item_entry_title)
                    )
                }
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                            navigateBack()
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null//stringResource(R.string.item_entry_title)
                    )
                }
            }
        }
    ) {
        InfusionDetailsBody(uiState = uiState.value, modifier = Modifier.padding(it))
    }
}

@Composable
fun InfusionDetailsBody(uiState: InfusionDetailsUiState, modifier: Modifier = Modifier) {
    Column {
        InfusionItemDetails(
            uiState.infusionDetails,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InfusionItemDetails(details: InfusionDetails, modifier: Modifier) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        ItemDetailsRow(labelResID = R.string.infusion_site, itemDetail = details.infusionSite)
        ItemDetailsRow(labelResID = R.string.treatment, itemDetail = details.treatment)
        ItemDetailsRow(labelResID = R.string.dose_units, itemDetail = details.doseUnits)
        ItemDetailsRow(
            labelResID = R.string.lot_number,
            itemDetail = details.lotNumber
        )
        ItemDetailsRow(labelResID = R.string.date, itemDetail = details.date)
        ItemDetailsRow(labelResID = R.string.time, itemDetail = details.time)

    }
}