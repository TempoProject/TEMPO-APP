package com.tempo.tempoapp.ui.bleeding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.Loading
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
    navController: NavController? = null,
) {
    val viewModel: BleedingDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = BleedingEventDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = { navController?.navigateUp() }
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
                            BleedingEventEditDestination.routeWithArgs.replace(
                                "{${BleedingEventEditDestination.itemIdArg}}",
                                uiState.value.bleedingDetails.id.toString()
                            )
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_event)
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteItem()
                            navController?.navigateUp()
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_event)
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
 * Composable function for displaying details of a single bleeding event item.
 */
@Composable
fun BleedingDetailsBody(uiState: BleedingDetailsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (uiState.isLoading) {
            Loading()
        } else {
            BleedingItemDetails(
                uiState.bleedingDetails,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BleedingItemDetails(details: BleedingDetails, modifier: Modifier) {
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
                    text = stringResource(R.string.event_details),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                ItemDetailsRow(
                    label = stringResource(R.string.event),
                    itemDetail = details.eventType.ifBlank { stringResource(R.string.not_specified) }
                )

                ItemDetailsRow(
                    label = stringResource(R.string.site_string_label),
                    itemDetail = details.site.ifBlank { stringResource(R.string.not_specified) }
                )

                ItemDetailsRow(
                    label = stringResource(R.string.cause_string_label),
                    itemDetail = details.cause.ifBlank { stringResource(R.string.not_specified) }
                )

                ItemDetailsRow(
                    label = stringResource(R.string.pain_scale_string_label),
                    itemDetail = "${details.painScale} / 10"
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
                    text = stringResource(R.string.treatment),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                ItemDetailsRow(
                    label = stringResource(R.string.did_you_treat_yself),
                    itemDetail = details.treatment.ifBlank { stringResource(R.string.not_specified) }
                )

                if (details.treatment == "Sì") {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

                    Text(
                        text = stringResource(R.string.details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (details.medicationType.isNotBlank()) {
                        ItemDetailsRow(
                            label = stringResource(R.string.drug_name),
                            itemDetail = details.medicationType
                        )
                    }

                    if (details.dose.isNotBlank()) {
                        ItemDetailsRow(
                            label = stringResource(R.string.dose_units),
                            itemDetail = details.dose
                        )
                    }

                    if (details.lotNumber.isNotBlank()) {
                        ItemDetailsRow(
                            label = stringResource(R.string.lot_number),
                            itemDetail = details.lotNumber
                        )
                    }
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
                        text = "Note",
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

/**
 * Composable function for displaying a single row of item details.
 */
@Composable
internal fun ItemDetailsRow(
    label: String,
    itemDetail: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = itemDetail,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
