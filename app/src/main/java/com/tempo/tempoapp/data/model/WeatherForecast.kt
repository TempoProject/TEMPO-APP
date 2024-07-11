package com.tempo.tempoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherForecast(
    @PrimaryKey(autoGenerate = false)
    val timestamp: Long = 0,
    @ColumnInfo(name = "weather")
    val main: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "temperture")
    val temp: Double,
    @ColumnInfo(name = "feels_like")
    val feelsLike: Double,
    @ColumnInfo(name = "temp_min")
    val tempMin: Double,
    @ColumnInfo(name = "temp_max")
    val tempMax: Double,
    @ColumnInfo(name = "pressure")
    val pressure: Int,
    @ColumnInfo(name = "humidity")
    val humidity: Int,
    @ColumnInfo(name = "wind_speed")
    val windSpeed: Double,
    @ColumnInfo(name = "wind_degree")
    val windDeg: Int,
    @ColumnInfo(name = "wind_gust")
    val windGust: Double,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = false
)

data class WeatherForecastToJson(
    val timestamp: Long = 0,
    val main: String,
    val description: String,
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val windGust: Double
)

fun WeatherForecast.toWeatherForecastToJson(timestamp: Long): WeatherForecastToJson =
    WeatherForecastToJson(
        timestamp = timestamp,
        main = main,
        description = description,
        temp = temp,
        feelsLike = feelsLike,
        tempMin = tempMin,
        tempMax = tempMax,
        pressure = pressure,
        humidity = humidity,
        windSpeed = windSpeed,
        windDeg = windDeg,
        windGust = windGust
    )


data class WeatherData(
    val coord: Coord,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)


data class Coord(
    val lon: Double,
    val lat: Double
)


data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)


data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int,
    val grnd_level: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)


data class Clouds(
    val all: Int
)


data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)