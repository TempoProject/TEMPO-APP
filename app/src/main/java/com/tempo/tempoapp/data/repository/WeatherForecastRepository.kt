package com.tempo.tempoapp.data.repository

import com.tempo.tempoapp.data.dao.WeatherForecastDao
import com.tempo.tempoapp.data.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

class WeatherForecastRepository(private val weatherForecastDao: WeatherForecastDao) :
    LogbookRepository<WeatherForecast> {
    override suspend fun insertItem(item: WeatherForecast): Long = weatherForecastDao.insert(item)

    override suspend fun deleteItem(item: WeatherForecast) = weatherForecastDao.delete(item)

    override suspend fun updateItem(item: WeatherForecast) = weatherForecastDao.update(item)

    override fun getAll(): Flow<List<WeatherForecast>> {
        TODO("Not yet implemented")
    }

    override fun getItemFromId(id: Int): Flow<WeatherForecast> {
        TODO("Not yet implemented")
    }

    suspend fun getAllUnsentWeatherForecasts(): List<WeatherForecast> {
        return weatherForecastDao.getAllUnsentWeatherForecasts()
    }
}