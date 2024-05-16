package com.tempo.tempoapp.movesense

import android.annotation.SuppressLint

typealias BluetoothDeviceInfo = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String
)

@SuppressLint("MissingPermission")
fun android.bluetooth.BluetoothDevice.toBluetootDeviceInfo(): BluetoothDeviceInfo {
    return BluetoothDevice(
        name = if (name == null) "NoName" else name,
        address = address
    )

}
