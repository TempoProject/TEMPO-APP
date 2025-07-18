package com.tempo.tempoapp.ui.prophylaxis

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.tempo.tempoapp.ui.InformationDialog
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.toStringDate
import com.tempo.tempoapp.ui.toStringTime
import kotlinx.coroutines.launch

object ProphylaxisDetailsScreenRoute : NavigationDestination {
    override val route: String
        get() = "prophylaxis_details"
    override val titleRes: Int
        get() = R.string.details

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProphylaxisDetailScreen(
    navController: NavController? = null
) {

    val viewModel: ProphylaxisDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = R.string.prophylaxis_details),
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
                        navController?.navigate("${ProphylaxisEditDestination.route}/${uiState.value.prophylaxisDetails.id}")
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
                        showCancelDialog = true
                        /*coroutineScope.launch {
                            val success = viewModel.deleteItem()
                            if (success)
                                navController?.navigateUp()
                        }*/
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
        ProphylaxisDetailsBody(
            uiState = uiState.value, Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        )
    }

    if (showCancelDialog) {
        InformationDialog(
            title = R.string.delete_prophylaxis,
            message = R.string.delete_prophylaxis_confirmation,
            confirm = R.string.confirm,
            cancel = R.string.cancel_action,
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                coroutineScope.launch {
                    val success = viewModel.deleteItem()
                    if (success)
                        navController?.navigateUp()
                }
            }
        )
    }
}

/**
 * Composable function for displaying details of a single bleeding event item.
 */
@Composable
fun ProphylaxisDetailsBody(uiState: ProphylaxisDetailsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (uiState.isLoading) {
            Loading()
        } else {
            ProphylaxisDetailsItem(uiState.prophylaxisDetails, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ProphylaxisDetailsItem(
    item: ProphylaxisDetails,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
            modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                /*Text(
                    text = stringResource(id = R.string.profylaxis_screen_title).removePrefix("Aggiungi ")
                        .removePrefix("Add ")
                        .replaceFirst("p", "P"),
                    style = MaterialTheme.typography.titleLarge
                )*/
                Column {
                    /*Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.treatment),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = item.,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }*/
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.drug_name),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = item.drugName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.dose_units),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = item.dosage.toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.infusion_performed),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(item.responded.mapResponse()),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Text(
                    text = if (item.responseDateTime != 0L) item.date.toStringDate() + " " + item.responseDateTime.toStringTime() else item.date.toStringDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(alignment = Alignment.End)
                )
            }
        }
    }
}

fun Int.mapResponse(): Int = when (this) {
    0 -> R.string.no
    1 -> R.string.yes
    else -> R.string.waiting_response
}

// unused
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
