package com.tempo.tempoapp.ui.infusion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object InfusionEditDestination : NavigationDestination {
    override val route: String
        get() = "infusion_edit"
    override val titleRes: Int
        get() = R.string.edit

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfusionEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: InfusionEditViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        TempoAppBar(
            title = stringResource(id = InfusionEditDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) {
        InfusionEventBody(
            uiState, viewModel::updateUiState, onSave = {
                coroutineScope.launch {
                    viewModel.update()
                    navigateBack()
                }
            },
            modifier = Modifier.padding(it)
        )
    }
}