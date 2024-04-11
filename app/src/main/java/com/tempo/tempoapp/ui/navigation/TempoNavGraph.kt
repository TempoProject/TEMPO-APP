package com.tempo.tempoapp.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tempo.tempoapp.data.healthconnect.HealthConnectManager
import com.tempo.tempoapp.ui.bleeding.BleedingDetailsScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEditScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEntryDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEntryScreen
import com.tempo.tempoapp.ui.bleeding.BleedingEventDetailsDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEventEditDestination
import com.tempo.tempoapp.ui.history.HistoryDestination
import com.tempo.tempoapp.ui.history.HistoryScreen
import com.tempo.tempoapp.ui.home.HomeDestination
import com.tempo.tempoapp.ui.home.HomeScreen
import com.tempo.tempoapp.ui.infusion.InfusionDetailsDestination
import com.tempo.tempoapp.ui.infusion.InfusionDetailsScreen
import com.tempo.tempoapp.ui.infusion.InfusionEditDestination
import com.tempo.tempoapp.ui.infusion.InfusionEditScreen
import com.tempo.tempoapp.ui.infusion.InfusionEntryDestination
import com.tempo.tempoapp.ui.infusion.InfusionEventScreen
import com.tempo.tempoapp.ui.reminders.ReminderDestination
import com.tempo.tempoapp.ui.reminders.ReminderScreen

@Composable
fun TempoNavHost(
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        /**
         * Home screen
         */
        composable(route = HomeDestination.route) {
            val availability by healthConnectManager.availability
            HomeScreen(navigateToBleedingEntry = { navController.navigate(BleedingEntryDestination.route) },
                availability = availability,
                onResumeAvailabilityCheck = {
                    healthConnectManager.checkAvailability()
                },
                navigateToInfusionEntry = { navController.navigate(InfusionEntryDestination.route) },
                navigateToInfusionUpdate = {
                    navController.navigate("${InfusionEntryDestination.route}/${it}")
                },
                navigateToBleedingUpdate = {
                    navController.navigate("${BleedingEventDetailsDestination.route}/${it}")
                },
                navigateToHistory = {
                    navController.navigate(HistoryDestination.route)
                },
                navigateToReminder = {
                    navController.navigate(ReminderDestination.route)
                })
        }
        /**
         * Add new bleeding event
         */
        composable(route = BleedingEntryDestination.route) {
            BleedingEntryScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
            )
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
            BleedingDetailsScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
                navigateToBleedingEdit =
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
            BleedingEditScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
            )
        }

        /**
         * Add new infusion event
         */
        composable(route = InfusionEntryDestination.route) {
            InfusionEventScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
            )
        }

        /**
         * Details infusion
         */
        composable(
            route = InfusionDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(InfusionDetailsDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            InfusionDetailsScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
                navigateToInfusionEdit = {
                    navController.navigate(
                        "${InfusionEditDestination.route}/${it}"
                    )
                }
            )
        }

        /**
         * Edit infusion
         */
        composable(
            route = InfusionEditDestination.routeWithArgs,
            arguments = listOf(navArgument(InfusionEditDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            InfusionEditScreen(
                onNavigateUp = { navController.navigateUp() },
                navigateBack = { navController.popBackStack() },
            )
        }

        /**
         * History
         */

        composable(
            route = HistoryDestination.route
        ) {
            HistoryScreen(
                navigateToInfusionUpdate = {
                    navController.navigate("${InfusionEntryDestination.route}/${it}")
                },
                navigateToBleedingUpdate = {
                    navController.navigate("${BleedingEventDetailsDestination.route}/${it}")
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = ReminderDestination.route
        ) {
            ReminderScreen(onNavigateUp = { navController.navigateUp() })
        }

    }
}