package com.tempo.tempoapp.ui.infusion

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
import androidx.compose.material3.Divider
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
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.bleeding.ItemDetailsRow
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import kotlinx.coroutines.launch

/**
 * Represents the destination for viewing infusion details.
 */
object InfusionDetailsDestination : NavigationDestination {
    override val route: String
        get() = "infusion_details"
    override val titleRes: Int
        get() = R.string.details

    const val itemIdArg = "itemId"
    val routeWithArgs = "${InfusionEntryDestination.route}/{$itemIdArg}"
}

/**
 * Composable function for displaying the screen to view infusion details.
 *
 * @param onNavigateUp Navigation callback for navigating up.
 * @param navigateToInfusionEdit Navigation callback for editing infusion details.
 * @param viewModel View model for managing infusion details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionDetailsScreen(
    navController: NavController? = null,
) {
    val viewModel: InfusionDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = InfusionDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = {
                    navController?.navigateUp()
                }
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
                    onClick = {
                        navController?.navigate(
                            InfusionEditDestination.routeWithArgs.replace(
                                "{${InfusionEditDestination.itemIdArg}}",
                                uiState.value.id.toString()
                            )
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_infusion)
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                        }.invokeOnCompletion {
                            navController?.navigateUp()
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_infusion)
                    )
                }
            }
        }
    ) { innerPadding ->
        InfusionDetailsBody(
            uiState = uiState.value,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        )
    }
}

/**
 * Composable function for displaying the body content of infusion details.
 *
 * @param uiState UI state containing infusion details.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun InfusionDetailsBody(uiState: InfusionDetailsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (uiState.isLoading) {
            Loading()
        } else {
            InfusionItemDetails(
                uiState.infusionDetails,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Composable function for displaying details of an infusion item.
 *
 * @param details Details of the infusion item.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun InfusionItemDetails(details: InfusionDetails, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = stringResource(R.string.infusion_details),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                ItemDetailsRow(
                    label = stringResource(R.string.reason),
                    itemDetail = details.reason.ifBlank { stringResource(R.string.not_specified) }
                )


                ItemDetailsRow(
                    label = stringResource(R.string.drug_name),
                    itemDetail = details.drugName.ifBlank { stringResource(R.string.not_specified) }
                )

                ItemDetailsRow(
                    label = stringResource(R.string.dose_iu_mg),
                    itemDetail = details.dose.ifBlank { stringResource(R.string.not_specified) }
                )

                ItemDetailsRow(
                    label = stringResource(R.string.date),
                    itemDetail = details.date.toStringDate()
                )

                ItemDetailsRow(
                    label = stringResource(R.string.time),
                    itemDetail = details.time
                )
            }
        }

        if (details.batchNumber.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text(
                        text = stringResource(R.string.batch_information),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    ItemDetailsRow(
                        label = stringResource(R.string.lot_number),
                        itemDetail = details.batchNumber
                    )
                }
            }
        }


        if (!details.note.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                ) {
                    Text(
                        text = stringResource(R.string.note),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    Text(
                        text = details.note,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                    )
                }
            }
        }
    }
}