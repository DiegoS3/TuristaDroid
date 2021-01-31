package android.com.diego.turistadroid.bbdd.apibbdd.services.weather

import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.RetrofitClient

object WeatherAPI {

    private const val API_URL = "https://api.openweathermap.org/"
    val API_KEY: String = "9b52b9134d03b436565d97fdbaf4411e"
    val UNITS: String = "metric"
    val LANG: String = "en"
    val service: WeatherREST
        get() = WeatherClient.getClient(API_URL)!!.create(WeatherREST::class.java)

}