package com.tempo.tempoapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movesense.mds.Mds
import com.movesense.mds.MdsException
import com.movesense.mds.MdsHeader
import com.movesense.mds.MdsResponseListener
import com.tempo.tempoapp.FirebaseRealtimeDatabase
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Accelerometer
import com.tempo.tempoapp.data.model.toAccelerometerFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * MovesenseWorker is a Worker class responsible for handling background tasks related to Movesense device.
 *
 * @param appContext The application context.
 * @param params The parameters to configure the worker.
 */
class MovesenseWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        private val TAG = MovesenseWorker::class.java.simpleName
    }

    // Movesense repository to access Movesense data
    private val movesenseRepository =
        (appContext.applicationContext as TempoApplication).container.movesenseRepository

    // Accelerometer repository to access accelerometer data
    private val accelerometerRepository =
        (appContext.applicationContext as TempoApplication).container.accelerometerRepository

    // Firebase database reference
    private val databaseRef =
        FirebaseRealtimeDatabase.instance


    // Movesense data service
    private val mds = Mds.builder().build(appContext)

    /**
     * Performs the background work to interact with Movesense device.
     *
     * @return The Result of the work.
     */
    override suspend fun doWork(): Result {

        // Get device information
        val deviceInfo = movesenseRepository.getDeviceInfo()

        // Process based on the state input
        when (val state = inputData.getInt("state", 2)) {
            1 -> {
                Log.d(TAG, "Configuring data logging...")
                // Configure data logging
                val jsonConfig = """
                           {
                                       "config": {
                                           "dataEntries": {
                                               "dataEntry": [
                                                   {
                                                       "path": "/Meas/Acc/13"
                                                   }
                                               ]
                                           }
                                       }
                                   }
                       """

                mds.put(
                    "suunto://${deviceInfo.name.split(" ").last()}/Mem/DataLogger/Config",
                    jsonConfig,
                    object : MdsResponseListener {
                        override fun onSuccess(data: String?, header: MdsHeader?) {
                            Log.d(javaClass.simpleName, data.toString())
                        }

                        override fun onError(p0: MdsException?) {
                            Log.e(javaClass.simpleName, p0.toString())

                        }
                    })
            }

            2, 3 -> {
                Log.d(TAG, if (state == 2) "Stopping" else "Starting" + " data logging...")
                mds.put(
                    "suunto://${
                        deviceInfo.name.split(" ").last()
                    }/Mem/DataLogger/State",
                    """{"newState": $state}""",
                    object : MdsResponseListener {
                        override fun onSuccess(data: String?, header: MdsHeader?) {
                            println(if (state == 2) "stop logging" else "start logging")
                            Log.d(javaClass.simpleName, data.toString())

                        }

                        override fun onError(p0: MdsException?) {
                            Log.e(javaClass.simpleName, p0.toString())

                        }
                    })
            }

            4 -> {
                Log.d(TAG, "Saving data...")
                mds.get(
                    "suunto://${
                        deviceInfo.name.split(" ").last()
                    }/Mem/Logbook/Entries/", "", object : MdsResponseListener {
                        override fun onSuccess(data: String?, header: MdsHeader?) {
                            println(data)
                            val jsonElement: JsonElement = JsonParser.parseString(data)
                            val jsonObject: JsonObject = jsonElement.asJsonObject
                            val ids = jsonObject
                                .getAsJsonObject("Content")
                                .getAsJsonArray("elements")

                            for (id in ids) {
                                println("Id: ${id.asJsonObject.get("Id")}")
                                mds.get(
                                    "suunto://MDS/Logbook/${
                                        deviceInfo.name.split(" ").last()
                                    }/byId/${id.asJsonObject.get("Id")}/Data",
                                    "",
                                    object : MdsResponseListener {
                                        override fun onSuccess(data: String?, header: MdsHeader?) {
                                            println(data)

                                            val jsonElement: JsonElement =
                                                JsonParser.parseString(data)
                                            val jsonObject: JsonObject = jsonElement.asJsonObject

                                            val accArray = jsonObject
                                                .getAsJsonObject("Meas")
                                                .getAsJsonArray("Acc")

                                            val xList = mutableListOf<Float>()
                                            val yList = mutableListOf<Float>()
                                            val zList = mutableListOf<Float>()
                                            for (acc in accArray) {
                                                val accObject = acc.asJsonObject
                                                val arrayAcc = accObject.getAsJsonArray("ArrayAcc")

                                                for (arrayAccElement in arrayAcc) {
                                                    val arrayAccObject =
                                                        arrayAccElement.asJsonObject
                                                    xList.add(arrayAccObject.get("x").asFloat)
                                                    yList.add(arrayAccObject.get("y").asFloat)
                                                    zList.add(arrayAccObject.get("z").asFloat)


                                                }
                                            }
                                            println("x: $xList, y: $yList, z: $zList")
                                            CoroutineScope(Dispatchers.IO).launch {
                                                accelerometerRepository.insertItem(
                                                    Accelerometer(
                                                        0,
                                                        xList.joinToString(", "),
                                                        yList.joinToString(", "),
                                                        zList.joinToString(", "),
                                                        System.currentTimeMillis()
                                                    )
                                                )
                                            }
                                        }

                                        override fun onError(p0: MdsException?) {
                                            println(p0)
                                        }
                                    }
                                )
                            }
                            println("delete...")
                            mds.delete("suunto://${
                                deviceInfo.name.split(" ").last()
                            }/Mem/Logbook/Entries/", null, object : MdsResponseListener {
                                override fun onSuccess(data: String?, header: MdsHeader?) {
                                    Log.d(javaClass.simpleName, data.toString())
                                }

                                override fun onError(p0: MdsException?) {
                                    Log.e(javaClass.simpleName, p0.toString())
                                }
                            })
                        }

                        override fun onError(p0: MdsException?) {
                            println(p0)
                        }
                    }
                )
            }

            5 -> {
                Log.d(TAG, "Deleting data from movesense device...")
                mds.delete("suunto://${
                    deviceInfo.name.split(" ").last()
                }/Mem/Logbook/Entries/", null, object : MdsResponseListener {
                    override fun onSuccess(data: String?, header: MdsHeader?) {
                        Log.d(javaClass.simpleName, data.toString())
                    }

                    override fun onError(p0: MdsException?) {
                        Log.e(javaClass.simpleName, p0.toString())
                    }
                })
            }

            6 -> {
                Log.d(TAG, "Sending data to Firebase...")
                CoroutineScope(Dispatchers.IO).launch {
                    val data = accelerometerRepository.getAllData(false)
                    val id = FirebaseInstallations.getInstance().id.await()
                    data.forEach { acc ->
                        databaseRef.child("accelerometer").child(id).child(acc.id.toString())
                            .setValue(acc.toAccelerometerFirebase(acc.id))

                        accelerometerRepository.updateItem(acc.copy(isSent = true))
                    }
                }
            }
        }
        return Result.success()
    }
}