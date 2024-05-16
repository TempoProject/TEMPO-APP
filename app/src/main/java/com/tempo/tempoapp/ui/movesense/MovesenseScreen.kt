package com.tempo.tempoapp.ui.movesense

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
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
import com.tempo.tempoapp.ui.navigation.NavigationDestination
import com.tempo.tempoapp.utils.MovesenseService
import com.tempo.tempoapp.workers.MovesenseSaveRecords
import com.tempo.tempoapp.workers.MovesenseWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


object MovesenseDestination : NavigationDestination {
    override val route: String
        get() = "movesense"
    override val titleRes: Int
        get() = R.string.movesense

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesenseScreen(
    context: Context,
    onNavigateScanDevices: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: MovesenseViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
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

    val isBluetoothEnabled: Boolean = bluetoothAdapter.isEnabled

    val enableBL =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

    val openSettings = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val canEnableBL = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms[Manifest.permission.BLUETOOTH_SCAN] == true
        } else true

        if (canEnableBL && !isBluetoothEnabled) enableBL.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )
    }

    when {
        ContextCompat.checkSelfPermission(
            LocalContext.current, Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            LocalContext.current, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            LocalContext.current, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED -> {

        }

        else -> {
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                }
            }
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
                onConnect = {
                    viewModel.updateUi(isWorking = true)
                    mds.connect(state.value.movesense.address, object : MdsConnectionListener {
                        override fun onConnect(p0: String?) {
                            println("onConnect: $p0")
                            //viewModel.stopScan()
                        }

                        override fun onConnectionComplete(p0: String?, p1: String?) {
                            println("onConnectionComplete: $p0")
                            scope.launch {
                                viewModel.updateInfoDevice(state.value.movesense.copy(isConnected = true))
                            }.invokeOnCompletion {
                                context.startForegroundService(
                                    Intent(
                                        context,
                                        MovesenseService::class.java
                                    )
                                )
                                viewModel.updateUi()
                            }


                            //viewModel.stopScan()
                            //navigateToMovesense()
                        }

                        override fun onError(p0: MdsException?) {
                            println("onError: $p0")
                            if (p0.toString().contains("BleDevice not among connected devices:"))
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
                            println("onDisconnect: $p0")
                            viewModel.updateUi(true)
                            mds.disconnect(state.value.movesense.address)
                            scope.launch {
                                viewModel.updateInfoDevice(
                                    state.value.movesense.copy(
                                        isConnected = false
                                    )
                                )
                            }.invokeOnCompletion { viewModel.updateUi() }

                            //onNavigateUp()
                            //viewModel.stopScan()
                        }
                    })
                },
                onConfigure = {
                    /*val configure =
                        OneTimeWorkRequestBuilder<MovesenseWorker>().addTag("configureMovesense")
                            .build()
                    WorkManager.getInstance(context).enqueue(configure)
    */

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
                    val flushData = OneTimeWorkRequestBuilder<MovesenseWorker>()
                        .setInputData(Data.Builder().putInt("state", 4).build())
                        .addTag("onFlush")
                        .build()
                    WorkManager.getInstance(context).enqueue(flushData)
                },
                onDelete = {
                    val delete =
                        OneTimeWorkRequestBuilder<MovesenseWorker>()
                            .setInputData(Data.Builder().putInt("state", 5).build())
                            .addTag("onDelete")
                            .build()
                    WorkManager.getInstance(context).enqueue(delete)
                },
                onForget = {
                    viewModel.updateUi(isWorking = true)
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


                }, Modifier.padding(it)
            )
    }
}

@Composable
fun MovesenseBody(
    state: State<MovesenseUiState>,
    onConnect: () -> Unit,
    onConfigure: () -> Unit,
    onDisconnect: () -> Unit,
    onFlush: () -> Unit,
    onDelete: () -> Unit,
    onForget: () -> Unit,
    modifier: Modifier
) {

    Column(modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onConnect, enabled = !state.value.movesense.isConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Connetti")
        }
        OutlinedButton(
            onClick = onConfigure,
            enabled = state.value.movesense.isConnected && !state.value.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Configura")
        }
        OutlinedButton(
            onClick = onDisconnect,
            enabled = state.value.movesense.isConnected && !state.value.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Disconnetti")
        }
        OutlinedButton(
            onClick = onFlush,
            enabled = state.value.movesense.isConnected && !state.value.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Flush della memoria")
        }
        OutlinedButton(
            onClick = onDelete,
            enabled = state.value.movesense.isConnected && !state.value.isWorking,
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
    }
}