package com.tempo.tempoapp.utils

import com.tempo.tempoapp.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


// TODO
private const val BASE_URL =
    BuildConfig.API_BASE_URL

private val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface ApiLoginService {
    @POST("deanonymization/patient/verify")
    suspend fun verifyPatient(@Body body: PatientVerify): Response<Unit>

    @POST("deanonymization/user/login")
    suspend fun sessionID(@Body body: UserLoginRequest): Response<UserLoginResponse>

}

object ApiLogin {
    val retrofitService: ApiLoginService by lazy {
        retrofit.create(ApiLoginService::class.java)
    }
}

data class PatientVerify(
    val pid: Int
)

data class UserLoginRequest(
    val password: String,
    val email: String
)

data class UserLoginResponse(
    val session: SessionData,
    val surname: String,
    val name: String,
    val email: String,
    val id: Int
)

data class SessionData(
    val sid: String
)

data class ApiErrorResponse(
    val status: Int,
    val message: String
)