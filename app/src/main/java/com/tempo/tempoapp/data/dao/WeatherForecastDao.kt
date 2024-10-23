package com.tempo.tempoapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tempo.tempoapp.data.model.WeatherForecast

@Dao
interface WeatherForecastDao: LogbookDao<WeatherForecast>{

    /**
     * Retrieves all weather forecasts from the database where is_sent is equal to false.
     *
     * @return A [List] emitting a list of [WeatherForecast] objects.
     */
    @Query(
        """
        SELECT * 
        FROM weather_forecast WHERE is_sent = 0"""
    )
    suspend fun getAllUnsentWeatherForecasts(): List<WeatherForecast>
}