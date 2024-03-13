package com.tempo.tempoapp.ui.bleeding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object BleedingEventDetailsDestination : NavigationDestination {
    override val route: String
        get() = "bleeding_details"
    override val titleRes: Int
        get() = R.string.details

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@Composable
fun BleedingDetailsScreen(
    navigateToBleedingEdit: (Int) -> Unit,
    viewModel: BleedingDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { navigateToBleedingEdit(uiState.value.id) },
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
                        } },
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
        BleedingDetailsBody(
            uiState.value,
            Modifier.padding(it)
        )
    }

}

@Composable
fun BleedingDetailsBody(uiState: BleedingDetailsUiState, modifier: Modifier = Modifier) {
    Column {
        BleedingItemDetails(uiState.bleedingDetails, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun BleedingItemDetails(details: BleedingDetails, modifier: Modifier) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        ItemDetailsRow(labelResID = R.string.site_string_label, itemDetail = details.site)
        ItemDetailsRow(labelResID = R.string.cause_string_label, itemDetail = details.cause)
        ItemDetailsRow(labelResID = R.string.severity_string_label, itemDetail = details.severity)
        ItemDetailsRow(
            labelResID = R.string.pain_scale_string_label,
            itemDetail = details.painScale
        )
        ItemDetailsRow(labelResID = R.string.date, itemDetail = details.date)
        ItemDetailsRow(labelResID = R.string.time, itemDetail = details.time)

    }
}

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
