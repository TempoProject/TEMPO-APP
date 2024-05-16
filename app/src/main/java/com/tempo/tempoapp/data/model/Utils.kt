package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utils")
data class Utils(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "latest_update")
    val latestUpdate: Long?
)

@Entity(tableName = "movesense")
data class Movesense(
    @PrimaryKey(autoGenerate = false)
    val address: String,
    @ColumnInfo(name = "name_device")
    val name: String,
    @ColumnInfo(name = "status")
    val isConnected: Boolean
)


@Entity(tableName = "accelerometer")
data class Accelerometer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "x_axis")
    val xAxis: String,
    @ColumnInfo(name = "y_axis")
    val yAxis: String,
    @ColumnInfo(name = "z_axis")
    val zAxis: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class AccelerometerJson(
    val id: Int = 0,
    val xAxis: String,
    val yAxis: String,
    val zAxis: String,
    val timestamp: Long
)

fun Accelerometer.toAccelerometerFirebase(id: Int = 0): AccelerometerJson =
    AccelerometerJson(id, xAxis, yAxis, zAxis, timestamp)
