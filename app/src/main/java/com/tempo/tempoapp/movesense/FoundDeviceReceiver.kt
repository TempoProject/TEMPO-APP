package com.tempo.tempoapp.movesense

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * BroadcastReceiver implementation for handling found Bluetooth devices.
 *
 * @property onDeviceFound Callback function to be invoked when a Bluetooth device is found.
 */
class FoundDeviceReceiver(private val onDeviceFound: (BluetoothDevice) -> Unit) :
    BroadcastReceiver() {

    /**
     * Called when a broadcast is received.
     *
     * @param context The context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        println("onReceive ")
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let(onDeviceFound)

            }
        }
    }
}