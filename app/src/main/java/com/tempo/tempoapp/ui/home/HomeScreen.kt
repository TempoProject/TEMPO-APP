package com.tempo.tempoapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.data.healthconnect.HealthConnectAvailability
import com.tempo.tempoapp.data.model.BleedingEvent
import com.tempo.tempoapp.data.model.InfusionEvent
import com.tempo.tempoapp.data.model.StepsRecord
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date


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
    navigateToInfusionUpdate: (Int) -> Unit
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
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.add_new_bleeding)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                                .invokeOnCompletion { navigateToBleedingEntry() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.infusion)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }.invokeOnCompletion {
                                navigateToInfusionEntry()
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = R.string.history)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }.invokeOnCompletion {
                                // TODO
                            }
                        }
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
                                        navigateToBleedingEntry()
                                        scope.launch {
                                            sheetState.hide()
                                        }.invokeOnCompletion {
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
                                            navigateToInfusionEntry()
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
                            homeUiState.stepsList,
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
fun HomeBody(
    bleedingEventList: List<BleedingEvent>,
    infusionEventList: List<InfusionEvent>,
    stepsList: List<StepsRecord>,
    modifier: Modifier = Modifier,
    onInfusionItemClick: (Int) -> Unit,
    onBleedingItemClick: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (bleedingEventList.isEmpty() && infusionEventList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_bleeding_event),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            //Text(text = stepsList.count().toString())
            EventsList(
                bleedingEventList,
                infusionEventList,
                stepsList,
                onInfusionItemClick = { onInfusionItemClick(it.id) },
                onBleedingItemClick = { onBleedingItemClick(it.id) },
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )

        }
    }
}

@Composable
fun EventsList(
    bleedingEventList: List<BleedingEvent>,
    infusionEventList: List<InfusionEvent>,
    stepsList: List<StepsRecord>,
    onInfusionItemClick: (InfusionEvent) -> Unit,
    onBleedingItemClick: (BleedingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(infusionEventList) {
            InfusionItem(item = it, modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .clickable { onInfusionItemClick(it) })
        }
        items(bleedingEventList) {
            BleedingItem(
                item = it,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onBleedingItemClick(it) }
            )
        }
        items(stepsList) {
            stepItem(
                steps = it,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}


@Composable
private fun stepItem(steps: StepsRecord, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = steps.steps.toString(),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = steps.startTime,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = steps.endTime,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(alignment = Alignment.End)
            )

        }
    }
}

@Composable
private fun BleedingItem(item: BleedingEvent, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.bleedingCause,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = item.bleedingSite,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = item.timestamp.toStringDate(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(alignment = Alignment.End)
            )

        }
    }
}

@Composable
private fun InfusionItem(item: InfusionEvent, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.treatment,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = item.infusionSite,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = item.timestamp.toStringDate(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(alignment = Alignment.End)
            )
        }
    }
}

private fun Long.toStringDate(): String = SimpleDateFormat("dd-MM-yyyy").format(Date(this))
