package com.tempo.tempoapp.ui.home

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.HomeBody
import com.tempo.tempoapp.ui.bleeding.BleedingEntryDestination
import com.tempo.tempoapp.ui.bleeding.BleedingEventDetailsDestination
import com.tempo.tempoapp.ui.infusion.InfusionEntryDestination
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisDetailsScreenRoute
import com.tempo.tempoapp.ui.prophylaxis.ProphylaxisScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Representing the home navigation destination.
 */
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

private const val TAG = "HomeScreen"

/**
 * Composable function for rendering the Home screen.
 *
 * @param modifier Modifier for customizing the layout.

 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    //viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    //availability: HealthConnectAvailability,
    //onResumeAvailabilityCheck: () -> Unit,
    //lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    /*
    navigateToBleedingEntry: () -> Unit,
    navigateToInfusionEntry: () -> Unit,
    navigateToBleedingUpdate: (Int) -> Unit,
    navigateToInfusionUpdate: (Int) -> Unit,
    navigateToHistory: () -> Unit,
    navigateToAddReminder: () -> Unit,
    navigateToReminderList: () -> Unit,
    navigateToScanDevices: () -> Unit,
    navigateToMovesense: () -> Unit,
     */
) {
    //val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)

    /*val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            viewModel.initialLoad()

        }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    var hasLocationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
            )
        }
    }

    val location =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions())
        { locationPermission ->
            //check location permission Fine and Coarse
            hasLocationPermission =
                locationPermission[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                        locationPermission[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        }

    fun checkPermissions() {

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        Log.d(TAG, "Can schedule exact alarms: $canScheduleExactAlarms")
        Log.d(TAG, "Has notification permission: $hasNotificationPermission")

        showDialog = !(hasNotificationPermission)
    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }*/

    /*val currentOnAvailabilityCheck by rememberUpdatedState(onResumeAvailabilityCheck)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnAvailabilityCheck()
                // splash screen to check if permission are granted
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }*/

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(id = R.string.app_name), modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavDrawerItem(
                    stringId = R.string.add_event,
                    icon = ImageVector.vectorResource(id = R.drawable.baseline_bloodtype_24),
                    scope = scope,
                    drawerState = drawerState,
                    {
                        navController?.navigate(
                            route = BleedingEntryDestination.route
                        )
                    }
                )
                NavDrawerItem(
                    stringId = R.string.add_infusion,
                    icon = ImageVector.vectorResource(id = R.drawable.baseline_medication_24),
                    scope = scope,
                    drawerState = drawerState
                ) {

                    navController?.navigate(
                        route = InfusionEntryDestination.route
                    )

                }
                /*NavDrawerItem(
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
                    drawerState = drawerState
                ) {
                    navController?.navigate(
                        route = HistoryDestination.route
                    )

                }*/
                NavDrawerItem(
                    stringId = R.string.profylaxis_screen_title,
                    icon = Icons.Default.DateRange,
                    scope = scope,
                    drawerState = drawerState
                ) {
                    navController?.navigate(
                        route = ProphylaxisScreen.route
                    )
                }
                /*NavDrawerItem(
                    stringId = R.string.movesense,
                    icon = Icons.Default.Build,
                    scope = scope,
                    drawerState = drawerState,
                    navDestination = {
                        if (homeUiState.movesense != null) {
                            navController?.navigate(MovesenseDestination.route)
                        } else
                            navController?.navigate(ScanDeviceDestination.route)
                    }

                )*/
                /* Exit button is commented out for now
                NavDrawerItem(
                    stringId = R.string.logout,
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    scope = scope,
                    drawerState = drawerState
                ) {
                    scope.launch {
                        context.preferences.setLoggedIn(false)
                        context.preferences.setUserId("")
                        context.preferences.clearProphylaxisConfig()
                        // TODO cancel all alarms and notifications
                        // TODO cancel all work requests
                        navController?.navigate(
                            route = "splash_screen" // TODO Replace with your actual logout route
                        ) {
                            popUpTo(HomeDestination.route) {
                                inclusive = true
                            }

                        }
                    }
                }*/
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
                                Log.d(TAG, "Drawer state: $isOpen")
                                if (isClosed) open() else close()
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
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,

                    ) {
                    // Sheet content
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                navController?.navigate(
                                    route = BleedingEntryDestination.route
                                )
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(stringResource(R.string.add_event))
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                navController?.navigate(
                                    route = InfusionEntryDestination.route
                                )
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }

                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(stringResource(R.string.add_infusion))
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            //Log.d(TAG, "Permissions granted: ${viewModel.permissionsGranted.value}")
            /*if (!hasLocationPermission) {
                PermissionDialog(
                    showDialog = !hasLocationPermission,
                    permission = LocationTextProvider(),
                    isPermanentlyDeclined = false,
                    onDismiss = { showDialog = !showDialog },
                    onOkClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            location.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                )
                            )
                        } else
                            location.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                    },
                    onGoToAppSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.parse("package:${context.packageName}")
                        intent.setData(uri)
                        settingsLauncher.launch(intent)
                    })
            }
            if (!hasNotificationPermission) {
                PermissionDialog(
                    showDialog = !hasNotificationPermission,
                    permission = NotificationTextProvider(),
                    isPermanentlyDeclined = !ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    onDismiss = { showDialog = !showDialog },
                    onOkClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onGoToAppSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.parse("package:${context.packageName}")
                        intent.setData(uri)
                        settingsLauncher.launch(intent)
                    })
            }
            if (!canScheduleExactAlarms) {
                PermissionDialog(
                    !canScheduleExactAlarms,
                    permission = AlarManagerTextProvider(),
                    isPermanentlyDeclined = true,
                    onDismiss = { },
                    onOkClick = { /** NOTHING**/ },
                    onGoToAppSettings = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        val uri =
                            Uri.fromParts("package", context.packageName, null)
                        intent.setData(uri)
                        settingsLauncher.launch(intent)

                    })
            }*/
            /*when (availability) {
                HealthConnectAvailability.INSTALLED, HealthConnectAvailability.NOT_SUPPORTED -> {
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
                                    Text(stringResource(R.string.add_infusion))
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
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
                                    Text(stringResource(R.string.add_event))
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
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
                        }*/
            HomeBody(
                homeUiState.bleedingList,
                homeUiState.infusionList,
                homeUiState.stepsCount,
                homeUiState.combinedEvents,
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                onInfusionItemClick = { id ->
                    Log.d(TAG, "Infusion item clicked: $id")
                    navController?.navigate("${InfusionEntryDestination.route}/${id}")
                },
                onBleedingItemClick = { id ->
                    Log.d(TAG, "Bleeding item clicked: $id")
                    navController?.navigate("${BleedingEventDetailsDestination.route}/${id}")
                },
                onProphylaxisItemClick = { id ->
                    Log.d(TAG, "Prophylaxis item clicked: $id")
                    navController?.navigate("${ProphylaxisDetailsScreenRoute.route}/${id}")
                },
            )
        } /*else {
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
                                    text = stringResource(id = R.string.health_connect_permission_required),
                                )
                            }
                            ElevatedButton(onClick = {
                                //permissionsLauncher.launch(viewModel.permission)
                            }) {
                                Text(text = stringResource(id = R.string.authorize))
                            }
                        }
                    }*/
    }
}

/*
HealthConnectAvailability.NOT_INSTALLED -> {
    Column(
        modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        NotInstalledMessage()
    }
}
}
}
}
}*/

/**
 * Composable function for rendering a navigation drawer item.
 *
 * @param stringId String resource ID for the label text.
 * @param icon Icon for the drawer item.
 * @param scope Coroutine scope for managing drawer state changes.
 * @param drawerState Drawer state object.
 * @param navDestination Lambda function to navigate to the corresponding destination.
 */
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


/**
 * Composable function for displaying a message when Health Connect is not installed.
 * It provides a clickable link to install Health Connect.
 */
/*
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
}*/