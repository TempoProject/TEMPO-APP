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
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.Accelerometer
import com.tempo.tempoapp.data.model.Movesense
import com.tempo.tempoapp.data.model.toAccelerometerFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MovesenseWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val movesenseRepository =
        (appContext.applicationContext as TempoApplication).container.movesenseRepository

    private val accelerometerRepository =
        (appContext.applicationContext as TempoApplication).container.accelerometerRepository

    private val databaseRef =
        (appContext.applicationContext as TempoApplication).database

    private val mds = Mds.builder().build(appContext)

    override suspend fun doWork(): Result {

        val deviceInfo = movesenseRepository.getDeviceInfo()

        when (val state = inputData.getInt("state", 2)) {
            1 -> {
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
        /* if (state != 2) {
             //configState(mds, deviceInfo, state)
             mds.get("suunto://${
                 deviceInfo.name.split(" ").last()
             }/Mem/DataLogger/State", null, object : MdsResponseListener {
                 override fun onSuccess(data: String?, header: MdsHeader?) {
                     val status = Gson().fromJson(data, Map::class.java)
                     if (status["Content"] != 2)
                         configState(mds, deviceInfo)
                     /*mds.put(
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
                         })*/
                 }

                 override fun onError(p0: MdsException?) {
                     TODO("Not yet implemented")
                 }

             })
             /*
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
                 })*/

             configState(mds, deviceInfo, state = 3)
         } else {
             //configState(mds, deviceInfo)
         }*/

        return Result.success()


    }

    private fun configState(mds: Mds, deviceInfo: Movesense, state: Int = 2) {
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

}