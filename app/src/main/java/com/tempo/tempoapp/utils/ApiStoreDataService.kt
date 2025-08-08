package com.tempo.tempoapp.utils

import com.tempo.tempoapp.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


private const val BASE_URL =
    BuildConfig.API_BASE_URL

private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface ApiStoreDataService {
    @GET("logs/{pid}")

    suspend fun getLogs(
        @Path("pid") pid: String,
        @Header("Authorization") sessionID: String
    ): String


    @POST("logs/{pid}")
    suspend fun postLogs(
        @Path("pid") pid: String,
        @Header("Authorization") sessionID: String,
        @Body data: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

}

object StoreDataApi {
    val retrofitService: ApiStoreDataService by lazy {
        retrofit.create(ApiStoreDataService::class.java)
    }
}