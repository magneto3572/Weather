package com.app.apptenx.ui


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.appten.data.model.weather
import com.app.appten.data.network.APIService
import com.app.appten.data.network.RetroInstance
import com.app.apptenx.data.model.forecast
import com.app.apptenx.data.network.NetworkState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    private var forecastda: MutableLiveData<forecast.forecastdata?>? = null
    private var weatherda: MutableLiveData<weather.weatherdata>? = null
    private val _networkState  = MutableLiveData<NetworkState>()

    var name = MutableLiveData("")
    var temp = MutableLiveData("")
    var day1 = MutableLiveData("")
    var day2 = MutableLiveData("")
    var day3 = MutableLiveData("")
    var day4 = MutableLiveData("")
    var day1temp = MutableLiveData("")
    var day2temp = MutableLiveData("")
    var day3temp = MutableLiveData("")
    var day4temp = MutableLiveData("")

    fun getweather(city: String): MutableLiveData<weather.weatherdata>? {
        _networkState.postValue(NetworkState.LOADING)
        if (weatherda == null) {
            weatherda = MutableLiveData<weather.weatherdata>()
            apiweather(city)
        }
        return weatherda
    }

    fun networkcheck() : MutableLiveData<NetworkState> {
     return _networkState
    }


    private fun apiweather(city: String) {
        val apiService = RetroInstance.retroClient.create(APIService::class.java)
        val call = apiService.getweather(city)

        call?.enqueue(object : Callback<weather.weatherdata?> {
            override fun onResponse(
                call: Call<weather.weatherdata?>,
                response: Response<weather.weatherdata?>
            ) {
                weatherda?.value = response.body()
                //_networkState.postValue(NetworkState.LOADED)
            }

            override fun onFailure(call: Call<weather.weatherdata?>, t: Throwable) {
                _networkState.postValue(NetworkState.ERROR)
            }

        })
    }


    fun getforecast(city: String): MutableLiveData<forecast.forecastdata?>? {
        _networkState.postValue(NetworkState.LOADING)
        if (forecastda == null) {
            forecastda = MutableLiveData<forecast.forecastdata?>()
            apiforecast(city)
        }
        return forecastda
    }

    fun apiforecast(city : String) {
        val apiService = RetroInstance.retroClient.create(APIService::class.java)
        val call = apiService.getforecast(city)
        call?.enqueue(object : Callback<forecast.forecastdata?> {
            override fun onResponse(
                call: Call<forecast.forecastdata?>,
                response: Response<forecast.forecastdata?>
            ) {
                forecastda?.value = response.body()
                //_networkState.postValue(NetworkState.LOADED)
            }

            override fun onFailure(call: Call<forecast.forecastdata?>, t: Throwable) {
                _networkState.postValue(NetworkState.ERROR)
            }
        })
    }
}