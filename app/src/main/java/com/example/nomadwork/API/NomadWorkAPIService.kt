package com.example.nomadwork.API

import android.content.Intent
import android.os.Build
import com.example.nomadwork.API.Models.BaseResponse
import com.example.nomadwork.API.request.*
import com.example.nomadwork.API.response.LoginResponse
import com.example.nomadwork.API.response.WSSuggestResponse
import com.example.nomadwork.Activity.LoginActivity
import com.example.nomadwork.BuildConfig
import com.example.nomadwork.models.WorkStation
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by FlavioMacedo on 01/09/19.
 */

class NomadWorkAPIService private constructor() {
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitLogError: Retrofit

    companion object {
        //PROD
        const val BASE_URL = "http://nomadwork.herokuapp.com/" //"http://10.0.2.2:3000/"

        fun api(): NomadWorkAPIService {
            return Singletone.get()
        }
    }

    // Singletone configuration
    private object Singletone {
        fun get(): NomadWorkAPIService {
            return NomadWorkAPIService()
        }
    }

    init {
        setupRetrofit()
    }

    private fun setupRetrofit() {
        val httpBuilder = OkHttpClient.Builder()

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        httpBuilder.addInterceptor(RequestInterceptor)
        httpBuilder.addInterceptor(logger)

        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, MyDateTypeAdapter())
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpBuilder
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()


        retrofitLogError = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder().addInterceptor(logger)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    // Request Interceptor. Use this to manipulate the request before send it.
    // Add Headers and other stuff here
    // Also use this as a global error handler
    private object RequestInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {
            val request = chain!!.request()!!.newBuilder()

            val url = chain.request().url()
            val currentDeviceTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date())
            request.addHeader("Content-Type", "application/json")
            request.addHeader("device_timestamp", currentDeviceTime)
            request.addHeader("apk_version", BuildConfig.VERSION_NAME)
            request.addHeader("android_version", Build.VERSION.RELEASE)
            request.addHeader("Access-Control-Allow-Origin", "*")

            val s = Session.get(null)
            if (s != null) {
                request.addHeader("authorization", s.token)
            }

            val response = chain.proceed(request.build())

            return response
        }
    }

    // Endpoints
    val verifyUserEmail = retrofit.create(VerifyUserEmailService::class.java)
    val userLogin = retrofit.create(UserLogin::class.java)
    val newUser = retrofit.create(RegisterNewUser::class.java)
    val workStationsList = retrofit.create(WorkStationList::class.java)
    val workStatinDetails = retrofit.create(WorkStationDetails::class.java)
    val workStationSuggest = retrofit.create(WorkStationSuggest::class.java)
    val workStationSeacrhList = retrofit.create(WorkStationSearchList::class.java)
    val enumServices = retrofit.create(EnumServices::class.java)

}

// Interfaces

interface VerifyUserEmailService {
    @GET("api/user/{email}")
    fun verifyEmail(@Path ("email") email: String): Single<retrofit2.Response<Void>>
}

interface UserLogin {
    @POST("api/user/login")
    fun login(@Body loginRequest: LoginRequest): Single<BaseResponse<LoginResponse>>
}

interface RegisterNewUser {
    @POST ("api/register/")
    fun registerUser (@Body registerRequest: RegisterRequest): Single<BaseResponse<LoginResponse>>
}

interface WorkStationList {
    @GET ("api/establishmment/{latLong}")
    fun workStationsList (@Path ("latLong") latLong: String): Observable<BaseResponse<List<WorkStation>>>
}

interface WorkStationDetails {
    @GET ("api/establishmment/{id}")
    fun workStationDetails(@Path("id") id: Int): Single<BaseResponse<com.example.nomadwork.models.WorkStationDetails>>
}

interface WorkStationSuggest {
    @POST ("api/establishmment")
    fun workStationSuggest(@Body wsSuggestRequest: WSSuggestRequest): Single<BaseResponse<WSSuggestResponse>>
}

interface WorkStationSearchList {
    @GET("api/establishmment/{term}")
    fun workStationSearchList(@Path("term") term: String): Single<BaseResponse<List<WorkStation>>>
}

interface EnumServices {
    @GET ("api/establishmment/so_pra_ver_enum")
    fun enumServices(): Observable<retrofit2.Response<Void>>
}

// Type Adapters
class MyDateTypeAdapter : TypeAdapter<Date>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Date?) {
        if (value == null)
            out.nullValue()
        else
            out.value(value.time)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): Date? {
        return if (`in` != null) {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                null
            } else {
                val date = Date(`in`.nextLong())
                date
            }
        } else {
            null
        }
    }
}