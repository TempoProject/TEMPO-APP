package com.tempo.tempoapp.movesense

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>>
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>>

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}