@file:OptIn(ExperimentalMaterial3Api::class)

package com.tempo.tempoapp

import AppPreferencesManager
import PermissionScreen
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import com.tempo.tempoapp.ui.onboarding.LoginScreen
import com.tempo.tempoapp.ui.onboarding.WelcomeScreen
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisDetailScreen
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisDetailsScreenRoute
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisEditDestination
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisEditScreen
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisScreen
import com.tempo.tempoapp.ui.theme.TempoAppTheme
import com.tempo.tempoapp.workers.GetStepsRecord
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * MainActivity for the Tempo app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppPreferencesManager(this)
        setContent {
            TempoAppTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                //val isFirstLaunch by preferences.isFirstLaunch.collectAsStateWithLifecycle(
                //  initialValue = true
                //)
                LaunchedEffect(Unit) {
                    val availabilityStatus =
                        HealthConnectClient.getSdkStatus(context, context.packageName)

                    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                        navController.navigate("health_connect_unavailability_screen")
                        {
                            popUpTo("splash_screen") {
                                inclusive = true
                            }
                        }
                    }
                    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                        // Optionally redirect to package installer to find a provider, for example:
                        val uriString =
                            "market://details?id=${context.packageName}&url=healthconnect%3A%2F%2Fonboarding"
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setPackage("com.android.vending")
                                data = uriString.toUri()
                                putExtra("overlay", true)
                                putExtra("callerId", context.packageName)
                            }
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash_screen" //if (isFirstLaunch) WelcomeScreen.route else "splash_screen",
                ) {
                    composable(WelcomeScreen.route) {
                        WelcomeScreen(navController)
                    }
                    composable("splash_screen") {
                        SplashScreen(navController)
                    }
                    composable("health_connect_unavailability_screen") {
                        HealthConnectUnavailabilityScreen()
                    }
                    composable(PermissionScreen.route) {
                        PermissionScreen(navController = navController)
                    }
                    composable(HomeDestination.route) {
                        HomeScreen(navController = navController)
                    }
                    composable(
                        route = BleedingEventDetailsDestination.routeWithArgs,
                        arguments = listOf(navArgument(BleedingEventDetailsDestination.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        BleedingDetailsScreen(
                            navController
                        )
                    }
                    composable(
                        route = BleedingEventEditDestination.routeWithArgs,
                        arguments = listOf(navArgument(BleedingEventEditDestination.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        BleedingEditScreen(
                            onNavigateUp = { navController.navigateUp() },
                        )
                    }
                    composable(HistoryDestination.route) {
                        HistoryScreen(
                            navController = navController,
                            onNavigateUp = { navController.navigateUp() },
                        )
                    }

                    composable(route = BleedingEntryDestination.route) {
                        BleedingEntryScreen(
                            onNavigateUp = { navController.navigateUp() },
                        )
                    }

                    composable(route = InfusionEntryDestination.route) {
                        InfusionEventScreen(
                            navController,
                            onNavigateUp = { navController.navigateUp() },
                        )
                    }
                    composable(
                        route = InfusionDetailsDestination.routeWithArgs,
                        arguments = listOf(navArgument(InfusionDetailsDestination.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        InfusionDetailsScreen(
                            navController
                        )
                    }

                    composable(
                        route = InfusionEditDestination.routeWithArgs,
                        arguments = listOf(navArgument(InfusionEditDestination.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        InfusionEditScreen(
                            navController
                        )
                    }
                    composable(LoginScreen.route) {
                        LoginScreen(navController = navController)
                    }

                    composable(ProphylaxisScreen.route) {
                        ProphylaxisScreen(navController = navController)
                    }

                    composable(
                        ProphylaxisDetailsScreenRoute.routeWithArgs,
                        arguments = listOf(navArgument(ProphylaxisDetailsScreenRoute.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        ProphylaxisDetailScreen(
                            navController
                        )
                    }

                    composable(
                        ProphylaxisEditDestination.routeWithArgs,
                        arguments = listOf(navArgument(ProphylaxisEditDestination.itemIdArg) {
                            type = NavType.IntType
                        })
                    ) {
                        ProphylaxisEditScreen(
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(navController: NavController? = null) {
    val context = LocalContext.current
    var isFirstLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isFirstLaunch = context.preferences.isFirstLaunch.first()

        Log.d("TempoApp", "Is first launch: $isFirstLaunch")
        if (isFirstLaunch)
            navController?.navigate(WelcomeScreen.route) {
                //popUpTo("splash_screen") {
                //   inclusive = true
                //}
            }
        else
            checkPermissionsAndRun(
                HealthConnectClient.getOrCreate(context),
                //isFirstLaunch,
                navController,
                context
            )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
    ) {
        Row(
            Modifier
                .padding(it)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectUnavailabilityScreen() {
    Scaffold(
        topBar = {
            TempoAppBar(
                title = "Tempo",
                canNavigateBack = false
            )
        },
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(it),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "HealthConnect SDK is not available on this device",
            )
        }
    }
}

@Preview
@Composable
fun HealthConnectUnavailabilityScreenPreview() {
    HealthConnectUnavailabilityScreen()
}

@SuppressLint("RestrictedApi")
suspend fun checkPermissionsAndRun(
    healthConnectClient: HealthConnectClient,
    navController: NavController?,
    context: Context
) {
    // TODO: Check if user is logged in via app preferences
    Log.d("TempoApp", "Checking permissions...")
    val allPermissionsGranted = checkAllPermissions(healthConnectClient, context)


    if (allPermissionsGranted) {
        Log.d("TempoApp", "All permissions granted, proceeding to next step...")
        // Se tutti i permessi sono stati concessi, verifica se l'utente è loggato oppure no.
        handlePermissionsGranted(navController, context)
    } else {
        Log.d("TempoApp", "Some permissions are missing, navigating to PermissionScreen...")
        // Alcuni permessi mancanti - vai alla schermata permessi
        navController?.navigate(PermissionScreen.route) {
            popUpTo("splash_screen") {
                inclusive = true
            }
        }
    }
}

suspend fun checkAllPermissions(
    healthConnectClient: HealthConnectClient,
    context: Context
): Boolean {
    // 1. Controllo Health Connect permissions
    val healthPermissionsGranted = checkHealthConnectPermissions(healthConnectClient)

    // 2. Controllo Standard permissions (location, bluetooth, notifications)
    val standardPermissionsGranted = checkStandardPermissions(context)

    return healthPermissionsGranted && standardPermissionsGranted
}

suspend fun checkHealthConnectPermissions(healthConnectClient: HealthConnectClient): Boolean {
    val granted = healthConnectClient.permissionController.getGrantedPermissions()
    val requiredHealthPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    )
    return granted.containsAll(requiredHealthPermissions)
}

private fun checkStandardPermissions(context: Context): Boolean {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            //Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

private suspend fun handlePermissionsGranted(
    navController: NavController?,
    context: Context
) {


    Log.d("TempoApp", "Handling permissions granted...")
    if (context.preferences.isLoggedIn.first() && context.preferences.userId.first() != null) {
        // Marca che non è più il primo avvio
        //val preferences = AppPreferencesManager(context)
        //preferences.setFirstLaunch(false)

        Log.d("TempoApp", "User is logged in, checking active prophylaxis...")

        val canScheduleNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else {
            true
        }

        if (context.preferences.isActiveProphylaxis.first() && canScheduleNotification) {

            val workManager = WorkManager.getInstance(context)

            //preferences = AppPreferencesManager(this)
            /**
             * Schedule periodic work for saving records.
             */
            workManager.enqueueUniquePeriodicWork(
                "GetStepsRecord",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<GetStepsRecord>(15, TimeUnit.MINUTES)
                    .build()
            )
            Log.d("TempoApp", "Active prophylaxis is enabled and can schedule notifications")
            navController?.navigate(HomeDestination.route) {
                popUpTo("splash_screen") {
                    inclusive = true
                }
            }
        } else {
            Log.d("TempoApp", "Active prophylaxis is disabled")
            navController?.navigate(ProphylaxisScreen.route) {
                popUpTo("splash_screen") {
                    inclusive = true
                }
            }
        }

        // Se non è loggato, vai alla schermata di login
        // navController?.navigate(LoginScreen.route) { ... }
    } else {
        Log.d("TempoApp", "User is not logged in, navigating to Login")
        navController?.navigate(LoginScreen.route) {
            popUpTo("splash_screen") {
                inclusive = true
            }
        }
    }
}