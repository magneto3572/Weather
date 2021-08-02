package com.app.appten.data.network

import com.app.appten.data.model.weather
import com.app.apptenx.data.model.forecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {

    @GET("weather?units=metric&APPID=9b8cb8c7f11c077f8c4e217974d9ee40")
    fun getweather(
        @Query("q") city: String
    ): Call<weather.weatherdata?>?

    @GET("forecast?units=metric&APPID=9b8cb8c7f11c077f8c4e217974d9ee40")
    fun getforecast(
        @Query("q") city: String
    ): Call<forecast.forecastdata?>?
}