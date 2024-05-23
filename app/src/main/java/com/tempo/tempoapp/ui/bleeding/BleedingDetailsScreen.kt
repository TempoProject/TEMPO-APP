package com.tempo.tempoapp.ui.bleeding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch

/**
 * Defines the destination for navigating to Bleeding Event Details.
 */
object BleedingEventDetailsDestination : NavigationDestination {
    override val route: String
        get() = "bleeding_details"
    override val titleRes: Int
        get() = R.string.details

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

/**
 * Composable function for displaying the details screen of a bleeding event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleedingDetailsScreen(
    onNavigateUp: () -> Unit,
    navigateBack: () -> Unit,
    navigateToBleedingEdit: (Int) -> Unit,
    viewModel: BleedingDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = BleedingEventDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.padding(
                    bottom = dimensionResource(id = R.dimen.padding_large),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                FloatingActionButton(
                    onClick = { navigateToBleedingEdit(uiState.value.id) },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null//stringResource(R.string.item_entry_title)
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                            navigateBack()
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null//stringResource(R.string.item_entry_title)
                    )
                }
            }
        }
    ) {
        BleedingDetailsBody(
            uiState.value,
            Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        )
    }

}

/**
 * Composable function for displaying the body of a bleeding event's details.
 */
@Composable
fun BleedingDetailsBody(uiState: BleedingDetailsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        BleedingItemDetails(
            uiState.bleedingDetails,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Composable function for displaying details of a single bleeding event item.
 */
@Composable
fun BleedingItemDetails(details: BleedingDetails, modifier: Modifier) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        ItemDetailsRow(
            labelResID = R.string.site_string_label,
            itemDetail = details.site,
            modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )
        ItemDetailsRow(
            labelResID = R.string.cause_string_label,
            itemDetail = details.cause,
            modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )
        ItemDetailsRow(
            labelResID = R.string.severity_string_label,
            itemDetail = details.severity,
            modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )
        ItemDetailsRow(
            labelResID = R.string.pain_scale_string_label,
            itemDetail = details.painScale,
            modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )
        ItemDetailsRow(
            labelResID = R.string.date, itemDetail = details.date.toStringDate(), modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )
        ItemDetailsRow(
            labelResID = R.string.time, itemDetail = details.time, modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen
                        .padding_medium
                )
            )
        )

    }
}

/**
 * Composable function for displaying a single row of item details.
 */
@Composable
internal fun ItemDetailsRow(
    @StringRes labelResID: Int, itemDetail: String, modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = itemDetail, fontWeight = FontWeight.Bold)
    }
}
