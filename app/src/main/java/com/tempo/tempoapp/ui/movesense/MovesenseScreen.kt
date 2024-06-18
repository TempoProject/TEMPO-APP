package com.tempo.tempoapp.ui.movesense

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.movesense.mds.Mds
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException
import com.movesense.mds.MdsHeader
import com.movesense.mds.MdsResponseListener
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.common.BluetoothLegacyTextProvider
import com.tempo.tempoapp.ui.common.BluetoothTextProvider
import com.tempo.tempoapp.ui.common.PermissionDialog
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.utils.MovesenseService
import com.tempo.tempoapp.workers.MovesenseSaveRecords
import com.tempo.tempoapp.workers.MovesenseWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


/**
 * Represents the navigation destination for the Movesense screen.
 */
object MovesenseDestination : NavigationDestination {
    override val route: String
        get() = "movesense"
    override val titleRes: Int
        get() = R.string.movesense

}

private val TAG = "MovesenseScreen"

/**
 * Represents the Movesense screen composable function.
 * This composable function displays the Movesense screen UI.
 *
 * @param lifecycleOwner The lifecycle owner used to observe the lifecycle of the screen.
 * @param onNavigateBack Callback function to navigate back from the Movesense screen.
 * @param viewModel The MovesenseViewModel used to manage data for the Movesense screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesenseScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateBack: () -> Unit,
    viewModel: MovesenseViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var canEnableBl by remember { mutableStateOf(false) }
    var canEnableGeo by remember { mutableStateOf(false) }
    var isPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }

    val state = viewModel.movesenseUiState.collectAsState()

    val scope = rememberCoroutineScope()
    val mds by lazy {
        Mds.builder().build(context)
    }

    val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter.isEnabled) }

    val enableBL =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

    val openSettings =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        isPermissionPermanentlyDenied = perms.any {
            !it.value && !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                it.key
            )
        }
        showDialog = isPermissionPermanentlyDenied
        Log.d(TAG, "Permissions: $perms")
        canEnableBl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms[Manifest.permission.BLUETOOTH_SCAN] == true
        } else
            perms[Manifest.permission.BLUETOOTH] == true


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            canEnableGeo =
                perms[Manifest.permission.ACCESS_FINE_LOCATION] == true && perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            isLocationEnabled = isLocationEnabled(context)

            if (canEnableGeo && !isLocationEnabled)
                enableBL.launch(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                )

        }

        if (canEnableBl && !isBluetoothEnabled)
            enableBL.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
    }

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canEnableBl = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
            showDialog = !(canEnableBl)
            isBluetoothEnabled = bluetoothAdapter.isEnabled
        } else {
            canEnableBl = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
            canEnableGeo = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            showDialog = !(canEnableBl && canEnableGeo)
            isBluetoothEnabled = bluetoothAdapter.isEnabled
            isLocationEnabled = isLocationEnabled(context)

        }

    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    DisposableEffect(context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }


    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = MovesenseDestination.titleRes),
                canNavigateBack = true,
                navigateUp = {
                    if (!state.value.isWorking)
                        onNavigateBack()
                }
            )
        },

        ) {
        if (state.value.isWorking)
            Loading()
        else
            MovesenseBody(
                state,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (canEnableBl) else (canEnableGeo && canEnableBl && isLocationEnabled),
                onConnect = {
                    Log.d(TAG, "onConnect: ${state.value.movesense.address}")
                    viewModel.updateUi(isWorking = true)
                    mds.connect(
                        state.value.movesense.address,
                        object : MdsConnectionListener {
                            override fun onConnect(p0: String?) {
                                Log.d(TAG, "onConnect: $p0")
                                //viewModel.stopScan()
                            }

                            override fun onConnectionComplete(
                                p0: String?,
                                p1: String?
                            ) {
                                Log.d(TAG, "onConnectionComplete: $p0, $p1")
                                scope.launch {
                                    viewModel.updateInfoDevice(
                                        state.value.movesense.copy(
                                            isConnected = true
                                        )
                                    )
                                }.invokeOnCompletion {
                                    context.startForegroundService(
                                        Intent(
                                            context,
                                            MovesenseService::class.java
                                        )
                                    )
                                    viewModel.updateUi()
                                }
                            }

                            override fun onError(p0: MdsException?) {
                                Log.e(TAG, "onError: $p0")
                                if (p0.toString()
                                        .contains("BleDevice not among connected devices:")
                                )
                                    scope.launch {
                                        viewModel.updateInfoDevice(
                                            state.value.movesense.copy(
                                                isConnected = true
                                            )
                                        )
                                    }.invokeOnCompletion {
                                        context.startForegroundService(
                                            Intent(
                                                context,
                                                MovesenseService::class.java
                                            )
                                        )
                                        viewModel.updateUi()
                                    } else
                                    viewModel.updateUi()
                            }

                            override fun onDisconnect(p0: String?) {
                                Log.d(TAG, "onDisconnect: $p0")
                                viewModel.updateUi(true)
                                mds.disconnect(state.value.movesense.address)
                                scope.launch {
                                    viewModel.updateInfoDevice(
                                        state.value.movesense.copy(
                                            isConnected = false
                                        )
                                    )
                                }.invokeOnCompletion { viewModel.updateUi() }
                            }
                        })
                },
                onConfigure = {
                    Log.d(TAG, "onConfigure: ${state.value.movesense.address}")
                    val startLogging = PeriodicWorkRequestBuilder<MovesenseSaveRecords>(
                        repeatInterval = 20,
                        TimeUnit.MINUTES
                    ).addTag("onConfigure").build()


                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "onConfigure",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        startLogging
                    )

                },
                onDisconnect = {
                    Log.d(TAG, "onDisconnect: ${state.value.movesense.address}")
                    mds.put(
                        "suunto://${
                            state.value.movesense.name.split(" ").last()
                        }/Mem/DataLogger/State",
                        """{"newState": 2}""",
                        object : MdsResponseListener {
                            override fun onSuccess(data: String?, header: MdsHeader?) {
                                Log.d(javaClass.simpleName, data.toString())
                                scope.launch {
                                    viewModel.updateInfoDevice(
                                        state.value.movesense.copy(
                                            isConnected = false
                                        )
                                    )
                                }
                                    .invokeOnCompletion {
                                        context.stopService(
                                            Intent(
                                                context,
                                                MovesenseService::class.java
                                            )
                                        )

                                        viewModel.updateUi(isWorking = false)
                                        WorkManager.getInstance(context)
                                            .cancelUniqueWork("onConfigure")
                                        mds.disconnect(state.value.movesense.address)
                                        //onNavigateUp()
                                    }

                            }

                            override fun onError(p0: MdsException?) {
                                Log.e(javaClass.simpleName, p0.toString())
                                scope.launch {
                                    viewModel.updateInfoDevice(
                                        state.value.movesense.copy(
                                            isConnected = false
                                        )
                                    )
                                }
                                    .invokeOnCompletion {

                                        context.stopService(
                                            Intent(
                                                context,
                                                MovesenseService::class.java
                                            )
                                        )
                                        viewModel.updateUi(isWorking = false)
                                        WorkManager.getInstance(context)
                                            .cancelUniqueWork("onConfigure")
                                        mds.disconnect(state.value.movesense.address)
                                        //onNavigateUp()
                                    }

                            }
                        })
                },
                onFlush = {
                    Log.d(TAG, "onFlush: ${state.value.movesense.address}")
                    val flushData = OneTimeWorkRequestBuilder<MovesenseWorker>()
                        .setInputData(Data.Builder().putInt("state", 4).build())
                        .addTag("onFlush")
                        .build()
                    WorkManager.getInstance(context).enqueue(flushData)
                },
                onDelete = {
                    Log.d(TAG, "onDelete: ${state.value.movesense.address}")
                    val delete =
                        OneTimeWorkRequestBuilder<MovesenseWorker>()
                            .setInputData(Data.Builder().putInt("state", 5).build())
                            .addTag("onDelete")
                            .build()
                    WorkManager.getInstance(context).enqueue(delete)
                },
                onForget = {
                    viewModel.updateUi(isWorking = true)
                    Log.d(TAG, "onForget: ${state.value.movesense.address}")
                    mds.put(
                        "suunto://${
                            state.value.movesense.name.split(" ").last()
                        }/Mem/DataLogger/State",
                        """{"newState": 2}""",
                        object : MdsResponseListener {
                            override fun onSuccess(data: String?, header: MdsHeader?) {
                                scope.launch {
                                    mds.disconnect(state.value.movesense.address)
                                    viewModel.deleteInfoDevice(state.value.movesense)
                                }
                                    .invokeOnCompletion {
                                        context.stopService(
                                            Intent(
                                                context,
                                                MovesenseService::class.java
                                            )
                                        )
                                        WorkManager.getInstance(context)
                                            .cancelUniqueWork("onConfigure")
                                        viewModel.updateUi(isWorking = false)
                                        onNavigateBack()
                                    }
                            }

                            override fun onError(p0: MdsException?) {
                                scope.launch {
                                    mds.disconnect(state.value.movesense.address)
                                    viewModel.deleteInfoDevice(state.value.movesense)
                                }
                                    .invokeOnCompletion {
                                        context.stopService(
                                            Intent(
                                                context,
                                                MovesenseService::class.java
                                            )
                                        )
                                        WorkManager.getInstance(context)
                                            .cancelUniqueWork("onConfigure")
                                        viewModel.updateUi(isWorking = false)
                                        onNavigateBack()
                                    }
                            }
                        })


                },
                getPermission = { checkPermissions() },
                Modifier.padding(it)
            )
        if (showDialog) {
            val isPermanentlyDeclined =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) !ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) else !ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.BLUETOOTH
                ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) && !ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            PermissionDialog(
                showDialog,
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) BluetoothTextProvider() else BluetoothLegacyTextProvider(),
                isPermanentlyDeclined = isPermanentlyDeclined,
                onDismiss = { showDialog = !showDialog },
                onOkClick = {
                    val permissionRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                        ) else arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    launcher.launch(permissionRequired)
                },
                onGoToAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.parse("package:${context.packageName}")
                    intent.setData(uri)
                    openSettings.launch(intent)
                })
        }
    }
}

/**
 * Composable function representing the body content of the Movesense screen.
 * Displays buttons for various actions related to Movesense devices.
 *
 * @param state State object holding Movesense UI state.
 * @param hasPermission Boolean indicating if required permissions are granted.
 * @param onConnect Lambda function to connect to Movesense device.
 * @param onConfigure Lambda function to configure Movesense device.
 * @param onDisconnect Lambda function to disconnect from Movesense device.
 * @param onFlush Lambda function to flush Movesense device memory.
 * @param onDelete Lambda function to delete Movesense device data.
 * @param onForget Lambda function to forget Movesense device.
 * @param getPermission Lambda function to request permissions.
 * @param modifier Modifier for applying layout attributes.
 */
@Composable
fun MovesenseBody(
    state: State<MovesenseUiState>,
    hasPermission: Boolean = false,
    onConnect: () -> Unit,
    onConfigure: () -> Unit,
    onDisconnect: () -> Unit,
    onFlush: () -> Unit,
    onDelete: () -> Unit,
    onForget: () -> Unit,
    getPermission: () -> Unit,
    modifier: Modifier
) {

    Column(modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onConnect, enabled = !state.value.movesense.isConnected && hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Connetti")
        }
        OutlinedButton(
            onClick = onConfigure,
            enabled = state.value.movesense.isConnected && !state.value.isWorking && hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Configura")
        }
        OutlinedButton(
            onClick = onDisconnect,
            enabled = state.value.movesense.isConnected && !state.value.isWorking && hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Disconnetti")
        }
        OutlinedButton(
            onClick = onFlush,
            enabled = state.value.movesense.isConnected && !state.value.isWorking && hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Flush della memoria")
        }
        OutlinedButton(
            onClick = onDelete,
            enabled = state.value.movesense.isConnected && !state.value.isWorking && hasPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cancella dati")
        }
        OutlinedButton(
            onClick = onForget,
            enabled = !state.value.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Dimentica il dispositivo")
        }
        if (!hasPermission)
            OutlinedButton(
                onClick = getPermission,
                enabled = !state.value.isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Concedi i permessi")
            }
    }
}