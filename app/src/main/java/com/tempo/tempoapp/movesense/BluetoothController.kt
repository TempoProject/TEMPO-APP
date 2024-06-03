package com.tempo.tempoapp.movesense

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining methods and properties for controlling Bluetooth functionality.
 */
interface BluetoothController {
    // Flow representing the list of scanned Bluetooth devices
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>>
    // Flow representing the list of paired Bluetooth devices
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>>

    /**
     * Starts the discovery process to find nearby Bluetooth devices.
     */
    fun startDiscovery()

    /**
     * Stops the ongoing discovery process.
     */
    fun stopDiscovery()

    /**
     * Releases any resources associated with the Bluetooth controller.
     */
    fun release()
}