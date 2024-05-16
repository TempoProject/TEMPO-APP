package com.tempo.tempoapp.movesense

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.movesense.mds.Mds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothController(private val context: Context) : BluetoothController {

    private val mds by lazy {
        Mds.builder().build(context)
    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetootDeviceInfo()
            println("device $newDevice")

            if (newDevice.name!!.contains(
                    "Movesense",
                    ignoreCase = true
                ) && newDevice !in devices
            ) devices + newDevice else devices
        }

    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceInfo>>
        get() = _scannedDevices.asStateFlow()


    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceInfo>>
        get() = _pairedDevices.asStateFlow()

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
         if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            println("no permission")
            return
        }
        println("permission")
        val x = context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        println("start discovery: $x")
        updatePairedDevices()
        bluetoothAdapter.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))
            return

        bluetoothAdapter.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    /*
        fun movesenseConnect(address: String) {
            mds.connect(address, object : MdsConnectionListener {
                override fun onConnect(p0: String?) {
                    println("onConnect: $p0")
                }

                override fun onConnectionComplete(p0: String?, p1: String?) {
                    println("onConnectionComplete: $p0")
                }

                override fun onError(p0: MdsException?) {
                    println("onError: $p0")
                }

                override fun onDisconnect(p0: String?) {
                    println("onDisconnect: $p0")
                }
            })
        }
     */


    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            return

        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetootDeviceInfo() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    fun hasPermission(permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}