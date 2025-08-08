package com.tempo.tempoapp.ui.bleeding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.InformationDialog
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

/**
 * Represents the destination for editing a bleeding event.
 */
object BleedingEventEditDestination : NavigationDestination {
    override val route: String
        get() = "bleeding_edit"
    override val titleRes: Int
        get() = R.string.edit

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"

}

/**
 * Composable function for displaying the screen for editing a bleeding event.
 * @param onNavigateUp Callback to handle the Up navigation action.
 * @param viewModel The view model for managing the UI logic of the edit screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleedingEditScreen(
    onNavigateUp: () -> Unit,
) {
    val viewModel: BleedingEditViewModel = viewModel(
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
                title = stringResource(id = BleedingEventEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp =
                    {
                        showExitDialog = true
                    },
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Loading()
            }
        } else {
            BleedingEventBody(
                uiState = uiState,
                onItemClick = viewModel::updateUiState,
                onSave = {
                    coroutineScope.launch {
                        val success = viewModel.update()
                        if (success) {
                            onNavigateUp()
                        }
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
            if (showExitDialog) {
                InformationDialog(
                    onConfirm = {
                        showExitDialog = false
                        onNavigateUp()
                    },
                    onDismiss = {
                        showExitDialog = false
                    }
                )
            }
        }
    }
}
