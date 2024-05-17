package com.tempo.tempoapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.TempoApplication
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
    navigateToHistory: () -> Unit,
    navigateToAddReminder: () -> Unit,
    navigateToReminderList: () -> Unit,
    navigateToScanDevices: () -> Unit,
    navigateToMovesense: () -> Unit,
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

    /**/
    val launcher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!TempoApplication.instance.alarm.canScheduleExactAlarms()) {
            if (ContextCompat.checkSelfPermission(
                    LocalContext.current,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) != PackageManager.PERMISSION_GRANTED
            )
                LaunchedEffect(Unit) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    val uri =
                        Uri.fromParts("package", TempoApplication.instance.packageName, null)
                    intent.setData(uri)
                    launcher1.launch(intent)
                }
        }
    }


    when {
        ContextCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        -> {
        }

        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
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
    var drawerIsEnabled by remember {
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

    ModalNavigationDrawer(
        gesturesEnabled = drawerIsEnabled,
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(id = R.string.app_name), modifier = Modifier.padding(16.dp))
                Divider()
                NavDrawerItem(
                    stringId = R.string.add_new_bleeding,
                    icon = ImageVector.vectorResource(id = R.drawable.baseline_bloodtype_24),
                    scope = scope,
                    drawerState = drawerState,
                    navigateToBleedingEntry
                )
                NavDrawerItem(
                    stringId = R.string.infusion,
                    icon = ImageVector.vectorResource(id = R.drawable.baseline_medication_24),
                    scope = scope,
                    drawerState = drawerState,
                    navigateToInfusionEntry
                )
                NavDrawerItem(
                    stringId = R.string.add_reminder,
                    icon = Icons.Default.Notifications,
                    scope = scope,
                    drawerState = drawerState,
                    navigateToAddReminder
                )
                NavDrawerItem(
                    stringId = R.string.history,
                    icon = Icons.Default.DateRange,
                    scope = scope,
                    drawerState = drawerState,
                    navigateToHistory
                )
                NavDrawerItem(
                    stringId = R.string.reminder,
                    icon = Icons.Default.List,
                    scope = scope,
                    drawerState = drawerState,
                    navigateToReminderList
                )
                NavDrawerItem(
                    stringId = R.string.movesense,
                    icon = Icons.Default.Build,
                    scope = scope,
                    drawerState = drawerState,
                    navDestination = {
                        if (homeUiState.movesense != null) {
                            navigateToMovesense()
                        } else
                            navigateToScanDevices()
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
                        if (drawerIsEnabled)
                            scope.launch {
                                drawerState.apply {
                                    Log.d(javaClass.simpleName, "drawer is closed? $isClosed")
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
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),

                    ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            },
        ) { innerPadding ->
            println("permission? ${viewModel.permissionsGranted.value}")

            when (availability) {
                HealthConnectAvailability.INSTALLED -> {
                    if (viewModel.permissionsGranted.value) {
                        drawerIsEnabled = true
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
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
                                            //viewModel.readStepsInterval()
                                        }.invokeOnCompletion {
                                            navigateToAddReminder()
                                            if (!sheetState.isVisible) {
                                                showBottomSheet = false
                                            }
                                        }
                                    }, modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(stringResource(id = R.string.add_reminder))
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
                    } else {
                        drawerIsEnabled = false
                        Column(
                            modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))) {
                                Text(
                                    text = "Autorizza l'accesso a health connect.",
                                )
                            }
                            ElevatedButton(onClick = {
                                permissionsLauncher.launch(viewModel.permission)
                            }) {
                                Text(text = "autorizza")
                            }
                        }
                    }
                }

                HealthConnectAvailability.NOT_INSTALLED -> {
                    Column(Modifier.padding(innerPadding)) {
                        NotInstalledMessage()
                    }
                    Toast.makeText(
                        LocalContext.current,
                        "Health connect not installed",
                        Toast.LENGTH_LONG
                    ).show()
                }

                HealthConnectAvailability.NOT_SUPPORTED -> {
                    drawerIsEnabled = true
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

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        sheetState.hide()
                                        //viewModel.readStepsInterval()
                                    }.invokeOnCompletion {
                                        navigateToAddReminder()
                                        if (!sheetState.isVisible) {
                                            showBottomSheet = false
                                        }
                                    }
                                }, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Text(stringResource(id = R.string.reminder))
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
            }

        }
    }
}

@Composable
private fun NavDrawerItem(
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


@Composable
private fun NotInstalledMessage() {
    // Build the URL to allow the user to install the Health Connect package
    val tag = stringResource(R.string.not_installed_tag)
    // Build the URL to allow the user to install the Health Connect package
    val url = Uri.parse(stringResource(id = R.string.market_url))
        .buildUpon()
        .appendQueryParameter("id", stringResource(id = R.string.health_connect_package))
        // Additional parameter to execute the onboarding flow.
        .appendQueryParameter("url", stringResource(id = R.string.onboarding_url))
        .build()
    val context = LocalContext.current

    val notInstalledText = stringResource(id = R.string.not_installed_description)
    val notInstalledLinkText = stringResource(R.string.not_installed_link_text)

    val unavailableText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
            append(notInstalledText)
            append("\n\n")
        }
        pushStringAnnotation(tag = tag, annotation = url.toString())
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(notInstalledLinkText)
        }
    }
    ClickableText(
        text = unavailableText,
        style = TextStyle(textAlign = TextAlign.Justify)
    ) { offset ->
        unavailableText.getStringAnnotations(tag = tag, start = offset, end = offset)
            .firstOrNull()?.let {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                )
            }
    }
}