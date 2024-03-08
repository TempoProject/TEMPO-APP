package com.tempo.tempoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tempo.tempoapp.ui.bleeding.BleedingEventDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEventScreen
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.home.HomeScreen

@Composable
fun TempoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(navigateToBleedingEntry = { navController.navigate(BleedingEventDestination.route) })
        }

        composable(route = BleedingEventDestination.route) {
            BleedingEventScreen()
        }

    }
}