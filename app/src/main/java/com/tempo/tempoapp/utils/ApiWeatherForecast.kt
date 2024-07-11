package com.tempo.tempoapp.utils

import com.tempo.tempoapp.BuildConfig
import com.tempo.tempoapp.data.model.WeatherData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BuildConfig.OPEN_WEATHER_URL)
    .build()

interface ApiWeatherForecast {
    @GET("weather")
    suspend fun getWeatherForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("units") units: String = "metric"
    ): Response<WeatherData>
}

object WeatherForecastApi {
    val retrofitService: ApiWeatherForecast by lazy {
        retrofit.create(ApiWeatherForecast::class.java)
    }
}
