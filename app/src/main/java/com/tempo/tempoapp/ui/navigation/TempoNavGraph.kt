package com.tempo.tempoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tempo.tempoapp.ui.bleeding.BleedingDetailsScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEditScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEntryDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEntryScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEventDetailsDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEventEditDestination
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.home.HomeScreen

@Composable
fun TempoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController, startDestination = HomeDestination.route, modifier = modifier
    ) {
        /**
         * Home screen
         */
        composable(route = HomeDestination.route) {
            HomeScreen(navigateToBleedingEntry = { navController.navigate(BleedingEntryDestination.route) },
                navigateToBleedingUpdate = {
                    navController.navigate("${BleedingEventDetailsDestination.route}/${it}")
                })
        }
        /**
         * Add new bleeding event
         */
        composable(route = BleedingEntryDestination.route) {
            BleedingEntryScreen()
        }

        /**
         * Update bleeding event
         */

        composable(
            route = BleedingEventDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(BleedingEventDetailsDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            BleedingDetailsScreen(navigateToBleedingEdit =
            { navController.navigate("${BleedingEventEditDestination.route}/${it}") })
        }

        /**
         * Edit bleeding event
         */

        composable(
            route = BleedingEventEditDestination.routeWithArgs,
            arguments = listOf(navArgument(BleedingEventEditDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            BleedingEditScreen()
        }


    }
}