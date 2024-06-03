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

/**
 * ViewModel class for managing Bluetooth device scanning and UI state.
 *
 * @param movesenseRepository Repository for handling Movesense device operations.
 * @param bluetoothController Bluetooth controller for managing Bluetooth operations.
 */
class ScanDevicesViewModel(
    private val movesenseRepository: MovesenseRepository,
    private val bluetoothController: AndroidBluetoothController,
) :
    ViewModel() {
    // MutableStateFlow to manage Bluetooth UI state
    private val _state = MutableStateFlow(BluetoothUiState())

    /**
     * Publicly exposed StateFlow of the Bluetooth UI state.
     * Combines the state from Bluetooth controller and internal state to provide a complete UI state.
     */
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

    /**
     * Starts Bluetooth device scanning.
     */
    fun startScan() = bluetoothController.startDiscovery()

    /**
     * Stops Bluetooth device scanning.
     */
    fun stopScan() = bluetoothController.stopDiscovery()

    /**
     * Updates the UI state to reflect whether Bluetooth scanning is in progress.
     *
     * @param isScanning Boolean indicating whether Bluetooth scanning is in progress.
     */
    fun updateUi(isScanning: Boolean = false) {
        _state.update {
            it.copy(isScanning = isScanning)
        }
    }

    /**
     * Saves the Movesense device information to the repository.
     *
     * @param item The Movesense device to be saved.
     */
    suspend fun saveDevice(item: Movesense) {
        movesenseRepository.insertItem(item)
    }

    /**
     * Checks whether the app has necessary Bluetooth permissions.
     *
     * @return True if the app has necessary Bluetooth permissions, false otherwise.
     */
    fun hasPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        bluetoothController.hasPermission(Manifest.permission.BLUETOOTH_SCAN)
    } else {
        bluetoothController.hasPermission(Manifest.permission.BLUETOOTH) && bluetoothController.hasPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) && bluetoothController.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }


}

/**
 * Data class representing the UI state of Bluetooth device scanning.
 *
 * @param scannedDevices List of scanned Bluetooth devices.
 * @param pairedDevices List of paired Bluetooth devices.
 * @param isScanning Boolean indicating whether Bluetooth scanning is in progress.
 */
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val pairedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val isScanning: Boolean = false
)