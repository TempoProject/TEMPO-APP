package com.tempo.tempoapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.ui.navigation.TempoNavHost

/**
 * Composable function for the Tempo app.
 */
@Composable
fun TempoApp(
    navController: NavHostController = rememberNavController(),
    healthConnectManager: HealthConnectManager,
) {
    TempoNavHost(navController = navController, healthConnectManager)
}

/**
 * Composable function for the Tempo app's app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}

) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            } else {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = null
                    )
                }
            }
        }
    )
}