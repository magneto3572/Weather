package com.app.appten.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroInstance {
    var BASE_URL = "https://api.openweathermap.org/data/2.5/"
    val retroClient: Retrofit
        get() {
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                val request = original.newBuilder()
                        .build()
                chain.proceed(request)
            })
            val client: OkHttpClient = httpClient.build()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
        }
}