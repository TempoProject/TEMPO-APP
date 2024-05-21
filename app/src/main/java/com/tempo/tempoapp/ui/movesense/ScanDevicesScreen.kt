package com.tempo.tempoapp.ui.movesense

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movesense.mds.Mds
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException
import com.tempo.tempoapp.R
import com.tempo.tempoapp.TempoAppBar
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.movesense.BluetoothDeviceInfo
import com.tempo.tempoapp.ui.AppViewModelProvider
import com.tempo.tempoapp.ui.Loading
import com.tempo.tempoapp.ui.common.BluetoothLegacyTextProvider
import com.tempo.tempoapp.ui.common.BluetoothTextProvider
import com.tempo.tempoapp.ui.common.PermissionDialog
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.utils.MovesenseService
import kotlinx.coroutines.launch


object ScanDeviceDestination : NavigationDestination {
    override val route: String
        get() = "scandevices"
    override val titleRes: Int
        get() = R.string.scandevices

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDevicesScreen(
    context: Context,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: ScanDevicesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToMovesense: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val localContext = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var canEnableBl by remember { mutableStateOf(false) }
    var canEnableGeo by remember { mutableStateOf(false) }
    var isPermissionPermanentlyDenied by remember { mutableStateOf(false) }

    val mds by lazy {
        Mds.builder().build(context)
    }

    val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    val isBluetoothEnabled: Boolean = bluetoothAdapter.isEnabled

    val state = viewModel.state.collectAsState()

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
                localContext as Activity,
                it.key
            )
        }
        showDialog = isPermissionPermanentlyDenied
        println("perms: $perms")
        canEnableBl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms[Manifest.permission.BLUETOOTH_SCAN] == true
        } else
            perms[Manifest.permission.BLUETOOTH] == true


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            canEnableGeo =
                perms[Manifest.permission.ACCESS_FINE_LOCATION] == true && perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (canEnableGeo)
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
        }

    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    DisposableEffect(context) {
        val observer = LifecycleEventObserver { _, event ->
            println(event)
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
                println(canEnableBl)
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }


    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TempoAppBar(
                title = stringResource(id = ScanDeviceDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },

        ) {

        if (state.value.isScanning)
            Loading()
        else
            DeviceScreen(
                state = state.value,
                startScan = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (canEnableBl)
                            viewModel.startScan()
                        else
                            showDialog = true
                    } else {
                        if (canEnableGeo && canEnableBl)
                            viewModel.startScan()
                        else
                            showDialog = true
                    }
                },
                stopScan = viewModel::stopScan,
                onClick = { device ->
                    viewModel.updateUi(true)
                    mds.connect(device.address, object : MdsConnectionListener {
                        override fun onConnect(p0: String?) {
                            println("onConnect: $p0")
                            viewModel.stopScan()
                        }

                        override fun onConnectionComplete(p0: String?, p1: String?) {
                            println("onConnectionComplete: $p0")
                            viewModel.stopScan()
                            context.startForegroundService(
                                Intent(
                                    context,
                                    MovesenseService::class.java
                                )
                            )
                            scope.launch {
                                viewModel.saveDevice(
                                    Movesense(
                                        device.address,
                                        device.name ?: "Movesense",
                                        isConnected = true
                                    )
                                )
                            }.invokeOnCompletion { err ->
                                try {
                                    viewModel.updateUi()
                                    navigateToMovesense()
                                } catch (e: Exception) {
                                    println(err.toString())
                                }

                            }

                        }

                        override fun onError(p0: MdsException?) {
                            println("onError: $p0")
                            viewModel.updateUi()
                            viewModel.stopScan()
                        }

                        override fun onDisconnect(p0: String?) {
                            println("onDisconnect: $p0")
                            viewModel.stopScan()
                            scope.launch {
                                viewModel.saveDevice(
                                    Movesense(
                                        device.address,
                                        device.name ?: "Movesense",
                                        isConnected = false
                                    )
                                )
                            }.invokeOnCompletion {
                                viewModel.updateUi()
                            }

                        }
                    })

                },
                Modifier.padding(it)
            )
        if (showDialog)
            PermissionDialog(
                showDialog,
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) BluetoothTextProvider() else BluetoothLegacyTextProvider(),
                isPermanentlyDeclined = isPermissionPermanentlyDenied,
                onDismiss = { showDialog = !showDialog },
                onOkClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                            )
                        )
                    else
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                },
                onGoToAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    println(TempoApplication.instance.packageName)
                    val uri = Uri.parse("package:${context.packageName}")
                    intent.setData(uri)
                    openSettings.launch(intent)
                })
    }

}

@Composable
fun DeviceScreen(
    state: BluetoothUiState,
    startScan: () -> Unit,
    stopScan: () -> Unit,
    onClick: (BluetoothDeviceInfo) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(modifier.fillMaxSize()) {
        BluetoothDeviceList(
            //pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            Arrangement.SpaceAround
        ) {
            Button(onClick = startScan) {
                Text(text = "Start scan")
            }
            Button(onClick = stopScan) {
                Text(text = "Stop scan")
            }
        }
    }
}

@Composable
fun BluetoothDeviceList(
    scannedDevices: List<BluetoothDeviceInfo>,
    onClick: (BluetoothDeviceInfo) -> Unit,
    modifier: Modifier = Modifier
) {

    var isEnabled by remember {
        mutableStateOf(true)
    }
    LazyColumn(modifier = modifier) {
        items(scannedDevices) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = it.name ?: "No Name"
                    )
                    Text(
                        text = it.address
                    )
                }
                Button(onClick = {
                    isEnabled = !isEnabled
                    onClick(it)
                }, enabled = isEnabled) {
                    Text(
                        text = "Connetti"
                    )
                }
            }
        }
    }
}