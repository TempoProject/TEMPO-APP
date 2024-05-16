package com.tempo.tempoapp.ui.movesense

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.repository.MovesenseRepository
import com.tempo.tempoapp.movesense.AndroidBluetoothController
import com.tempo.tempoapp.movesense.BluetoothDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ScanDevicesViewModel(
    private val movesenseRepository: MovesenseRepository,
    private val bluetoothController: AndroidBluetoothController,
) :
    ViewModel() {
    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _state.value
    )

    fun startScan() = bluetoothController.startDiscovery()

    fun stopScan() = bluetoothController.stopDiscovery()

    fun updateUi(isScanning: Boolean = false) {
        _state.update {
            it.copy(isScanning = isScanning)
        }
    }

    suspend fun saveDevice(item: Movesense) {
        movesenseRepository.insertItem(item)
    }

    fun hasPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        bluetoothController.hasPermission(Manifest.permission.BLUETOOTH_SCAN)
    } else {
        true
    }


}

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val pairedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val isScanning: Boolean = false
)