package com.tempo.tempoapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.data.healthconnect.HealthConnectAvailability
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.HomeBody
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    availability: HealthConnectAvailability,
    onResumeAvailabilityCheck: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navigateToBleedingEntry: () -> Unit,
    navigateToInfusionEntry: () -> Unit,
    navigateToBleedingUpdate: (Int) -> Unit,
    navigateToInfusionUpdate: (Int) -> Unit,
    navigateToHistory: () -> Unit
) {
    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            viewModel.initialLoad()
        }
    var hasNotificationPermission by remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    when {
        ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED -> {

        }

        else ->
            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentOnAvailabilityCheck by rememberUpdatedState(onResumeAvailabilityCheck)
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnAvailabilityCheck()
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    if (viewModel.permissionsGranted.value) {
        ModalNavigationDrawer(
            drawerState = drawerState, drawerContent = {
                ModalDrawerSheet {
                    Text(stringResource(id = R.string.app_name), modifier = Modifier.padding(16.dp))
                    Divider()
                    NavDrawerItem(
                        stringId = R.string.add_new_bleeding,
                        icon = Icons.Default.Create,
                        scope = scope,
                        drawerState = drawerState,
                        navigateToBleedingEntry
                    )
                    NavDrawerItem(
                        stringId = R.string.infusion,
                        icon = Icons.Default.Create,
                        scope = scope,
                        drawerState = drawerState,
                        navigateToInfusionEntry
                    )

                    NavDrawerItem(
                        stringId = R.string.history,
                        icon = Icons.Default.DateRange,
                        scope = scope,
                        drawerState = drawerState,
                        navigateToHistory
                    )
                }
            }) {
            Scaffold(
                topBar = {
                    TempoAppBar(
                        title = stringResource(id = HomeDestination.titleRes),
                        canNavigateBack = false,
                        navigateUp = {
                            scope.launch {
                                drawerState.apply {
                                    println(isClosed)
                                    if (isClosed)
                                        open()
                                    else close()
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showBottomSheet = !showBottomSheet },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                },
            ) { innerPadding ->
                when (availability) {
                    HealthConnectAvailability.INSTALLED -> {
                        if (showBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showBottomSheet = false
                                },
                                sheetState = sheetState,
                                modifier = Modifier.fillMaxHeight(fraction = 0.3f)
                            ) {
                                // Sheet content
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
                                        }.invokeOnCompletion {
                                            navigateToInfusionEntry()
                                            if (!sheetState.isVisible) {
                                                showBottomSheet = false
                                            }

                                        }
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text("Aggiungi infusione")
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
                                            //viewModel.readStepsInterval()
                                        }.invokeOnCompletion {
                                            navigateToBleedingEntry()
                                            if (!sheetState.isVisible) {
                                                showBottomSheet = false
                                            }
                                        }
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text("Aggiungi Sanguinamento")
                                }
                            }
                        }
                        HomeBody(
                            homeUiState.bleedingList,
                            homeUiState.infusionList,
                            homeUiState.stepsCount,
                            //homeUiState.stepsList,
                            modifier = modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                            onInfusionItemClick = navigateToInfusionUpdate,
                            onBleedingItemClick = navigateToBleedingUpdate
                        )


                    }

                    HealthConnectAvailability.NOT_INSTALLED -> {
                        // TODO landing page,
                        // redirect user to play store
                    }

                    HealthConnectAvailability.NOT_SUPPORTED -> {}
                }
            }
        }
    } else {
        ElevatedButton(onClick = {
            permissionsLauncher.launch(viewModel.permission)
        }) {
            Text(text = "autorizza")
        }
    }
}

@Composable
fun NavDrawerItem(
    @StringRes stringId: Int,
    icon: ImageVector,
    scope: CoroutineScope,
    drawerState: DrawerState,
    navDestination: () -> Unit = {}
) {
    NavigationDrawerItem(
        label = { Text(text = stringResource(id = stringId)) },
        icon = { Icon(icon, contentDescription = null) },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
                .invokeOnCompletion {
                    navDestination()
                }
        }
    )
}