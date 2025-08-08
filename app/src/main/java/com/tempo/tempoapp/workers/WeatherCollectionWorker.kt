package com.tempo.tempoapp.workers

import AppPreferencesManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tempo.tempoapp.TempoApplication
import com.tempo.tempoapp.data.model.WeatherForecast
import com.tempo.tempoapp.utils.CrashlyticsHelper
import com.tempo.tempoapp.utils.StoreDataApi
import com.tempo.tempoapp.utils.WeatherForecastApi
import kotlinx.coroutines.flow.first
import java.time.Instant


class WeatherCollectionWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val weatherForecastRepository =
        (ctx as TempoApplication).container.weatherForecastRepository
    private val locationManager =
        ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val preferences = AppPreferencesManager(applicationContext)

    override suspend fun doWork(): Result {
        return try {
            collectWeatherForecast()

            val pid = preferences.userId.first() ?: return Result.failure()
            val sessionId = preferences.sessionId.first() ?: return Result.failure()

            val weatherForecasts =
                weatherForecastRepository.getAllUnsentWeatherForecasts()

            weatherForecasts.forEach { weatherForecast ->
                StoreDataApi.retrofitService.postLogs(
                    pid,
                    sessionId,
                    mapOf(
                        "timestamp" to weatherForecast.timestamp,
                        "weather" to weatherForecast.main,
                        "description" to weatherForecast.description,
                        "temp" to weatherForecast.temp,
                        "feels_like" to weatherForecast.feelsLike,
                        "temp_min" to weatherForecast.tempMin,
                        "temp_max" to weatherForecast.tempMax,
                        "pressure" to weatherForecast.pressure,
                        "humidity" to weatherForecast.humidity,
                        "wind_speed" to weatherForecast.windSpeed,
                        "wind_degree" to weatherForecast.windDeg,
                        "wind_gust" to weatherForecast.windGust,
                    )
                )

                weatherForecastRepository.updateItem(weatherForecast.copy(isSent = true))
            }
            CrashlyticsHelper.logCriticalAction(
                action = "weather_collection_worker",
                success = true,
                details = "Weather data collected successfully"
            )

            Result.success()

        } catch (e: Exception) {
            CrashlyticsHelper.logCriticalAction(
                action = "weather_collection_worker",
                success = false,
                details = "Weather collection failed: ${e.message}"
            )
            Result.failure()
        }
    }

    private suspend fun collectWeatherForecast() {
        if (!hasLocationPermission()) {
            CrashlyticsHelper.logCriticalAction(
                action = "weather_collection_worker",
                success = false,
                details = "Location permission not granted"
            )
            return
        }

        val location = getCurrentLocation()
        if (location == null) {
            CrashlyticsHelper.logCriticalAction(
                action = "weather_collection_worker",
                success = false,
                details = "Could not get current location"
            )
            return
        }

        val weatherResponse = WeatherForecastApi.retrofitService.getWeatherForecast(
            lat = location.latitude.toString(),
            lon = location.longitude.toString()
        )


        if (weatherResponse.isSuccessful) {
            weatherResponse.body()?.let { weatherData ->
                val weatherForecast = WeatherForecast(
                    timestamp = Instant.now().toEpochMilli(),
                    main = weatherData.weather.firstOrNull()?.main ?: "",
                    description = weatherData.weather.firstOrNull()?.description ?: "",
                    temp = weatherData.main.temp,
                    feelsLike = weatherData.main.feels_like,
                    tempMin = weatherData.main.temp_min,
                    tempMax = weatherData.main.temp_max,
                    pressure = weatherData.main.pressure,
                    humidity = weatherData.main.humidity,
                    windSpeed = weatherData.wind.speed,
                    windDeg = weatherData.wind.deg,
                    windGust = weatherData.wind.gust,
                    isSent = false
                )


                weatherForecastRepository.insertItem(weatherForecast)


            }
        } else {
            CrashlyticsHelper.logCriticalAction(
                action = "weather_collection_worker",
                success = false,
                details = "Weather API failed: ${weatherResponse.code()}"
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                return null
            }

            val providers = listOf(LocationManager.NETWORK_PROVIDER)
                .filter { locationManager.isProviderEnabled(it) }

            if (providers.isEmpty()) {
                return null
            }

            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return location
                }
            }

            null

        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}