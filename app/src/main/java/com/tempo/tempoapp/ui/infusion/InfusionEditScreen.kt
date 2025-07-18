package com.tempo.tempoapp.ui.infusion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.InformationDialog
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

/**
 * Represents the destination for editing infusion details.
 */
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
    navController: NavController? = null,
) {
    val viewModel: InfusionEditViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = InfusionEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = {
                    showExitDialog = true
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Loading()
        } else {
            InfusionEventBody(
                uiState = uiState,
                onItemClick = viewModel::updateUiState,
                onSave = {
                    coroutineScope.launch {
                        val success = viewModel.update()
                        if (success) {
                            navController?.navigateUp()
                        }
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
            if (showExitDialog) {
                InformationDialog(
                    onConfirm = {
                        showExitDialog = false
                        navController?.navigateUp()
                    },
                    onDismiss = {
                        showExitDialog = false
                    }
                )
            }
        }
    }
}