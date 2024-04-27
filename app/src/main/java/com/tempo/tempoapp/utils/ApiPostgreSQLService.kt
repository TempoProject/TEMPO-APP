package com.tempo.tempoapp.utils

import com.tempo.tempoapp.data.model.BleedingEventJson
import com.tempo.tempoapp.data.model.InfusionEventJson
import com.tempo.tempoapp.data.model.StepsRecordToJson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


private const val BASE_URL =
    "http://192.168.1.37:3000"

private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface ApiPostgreSQLService {
    @GET("bleeding_event")
    suspend fun getBleedingEvents(): String

    @POST("bleeding_event")
    suspend fun postBleedingEvent(@Body bleedingEvent: BleedingEventJson): Response<Void>

    @POST("infusion_event")
    suspend fun postInfusionEvent(@Body infusionEventJson: InfusionEventJson): Response<Void>

    @POST("steps")
    suspend fun postSteps(@Body stepsRecordToJson: StepsRecordToJson): Response<Void>
}

object PostgresApi {
    val retrofitService: ApiPostgreSQLService by lazy {
        retrofit.create(ApiPostgreSQLService::class.java)
    }
}