package com.tempo.tempoapp.ui.bleeding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object BleedingEventEditDestination : NavigationDestination {
    override val route: String
        get() = "bleeding_edit"
    override val titleRes: Int
        get() = R.string.edit

    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"

}

@Composable
fun BleedingEditScreen(
    viewModel: BleedingEditViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    Scaffold {
        BleedingEventBody(uiState, viewModel::updateUiState, onSave = {
            coroutineScope.launch {
                viewModel.update()
            }
        },
            modifier = Modifier.padding(it))
    }
}
