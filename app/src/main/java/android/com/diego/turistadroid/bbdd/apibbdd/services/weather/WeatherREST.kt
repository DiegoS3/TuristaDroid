package android.com.diego.turistadroid.bbdd.apibbdd.services.weather

import android.com.diego.turistadroid.bbdd.apibbdd.entities.weather.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherREST {

    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String, @Query("lon") lon: String,
        @Query("APPID") app_id: String, @Query("units") units: String,
        @Query("lang") lang: String,
    ): Call<WeatherResponse>

}