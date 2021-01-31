package android.com.diego.turistadroid.navigation_drawer.ui.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.weather.WeatherResponse
import android.com.diego.turistadroid.bbdd.apibbdd.services.weather.WeatherAPI
import android.com.diego.turistadroid.utilities.Utilities.toast
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WeatherFragment : Fragment() {


    private lateinit var loadingView: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialog()
        loadingView.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_weather, container, false)
        getCurrentLocation(root)
        return root
    }

    private fun initDialog(){
        val builder = AlertDialog.Builder(context!!)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog_weather)
        loadingView = builder.create()
    }

    //Recuperamos localizacion actual del usuario
    private fun getCurrentLocation(view: View){
        val locationRequest = LocationRequest()
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(activity!!)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    //LocationServices.getFusedLocationProviderClient(activity!!)
                    //if (locationResult.locations.size > 0) {
                    for (location in locationResult.locations) {
                        //Obtenemos la ultima posicion conocida
                        val latestLocationIndex = locationResult.locations.size - 1
                        val latitud = locationResult.locations[latestLocationIndex].latitude //LATITUD
                        val longitud = locationResult.locations[latestLocationIndex].longitude //LONGITUD
                        val currentLocation = LatLng(latitud, longitud)
                        obtenerInformacionTiempo(view, currentLocation)
                        Log.i("currentLocation", "$latitud, $longitud")
                    }
                }
            }, Looper.getMainLooper())
    }

/**
 * Obtiene información del Tiempo
 */
private fun obtenerInformacionTiempo(view: View, currentLoc: LatLng) {

    val clientREST = WeatherAPI.service
    val call = clientREST.getCurrentWeatherData(
        currentLoc.latitude.toString(),
        currentLoc.longitude.toString(),
        WeatherAPI.API_KEY,
        WeatherAPI.UNITS,
        WeatherAPI.LANG)

    call.enqueue(object : Callback<WeatherResponse> {
        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
            if (response.isSuccessful) {
                val weatherResponse = response.body()!!
                Log.i("Datos", "Entro")
                val address = weatherResponse.name + ",  " + weatherResponse.sys?.country
                view.findViewById<TextView>(R.id.address).text = address
                //tiempoCiudad.text = weatherResponse.name
                //tiempoPais.text = weatherResponse.sys?.country
                var sdf = SimpleDateFormat("HH:mm   dd/MM/yyyy")
                var date = Date((weatherResponse.dt.toLong() * 1000))
                view.findViewById<TextView>(R.id.updated_at).text = "Updated at: " + sdf.format(date)
                view.findViewById<TextView>(R.id.temp).text = weatherResponse.main?.temp!!.toInt().toString() + "ºC"

                val imageV = view.findViewById<ImageView>(R.id.weatherImage)
                Glide.with(context!!)
                    .load("https://openweathermap.org/img/wn/" + weatherResponse.weather[0].icon + "@2x.png")
                    .override(300, 300)
                    .into(imageV)

                view.findViewById<TextView>(R.id.status).text = weatherResponse.weather[0].description.toString().capitalize()
                val tempMax = "Max Temp: " + weatherResponse.main?.temp_max!!.toInt() + "ºC"
                val tempMin = "Min Temp: " + weatherResponse.main?.temp_min!!.toInt() + "ºC"
                view.findViewById<TextView>(R.id.temp_min).text = tempMin
                view.findViewById<TextView>(R.id.temp_max).text = tempMax
                view.findViewById<TextView>(R.id.humidity).text = weatherResponse.main?.humidity.toString() + " %"
                view.findViewById<TextView>(R.id.pressure).text = weatherResponse.main?.pressure!!.toInt().toString() + " mBar"
                view.findViewById<TextView>(R.id.wind).text = weatherResponse.wind!!.speed.toString() + " km/h"
                //tiempoVisibilidad.text = "Visibilidad: " + weatherResponse.visibility + " m"
                sdf = SimpleDateFormat("HH:mm")
                date = Date((weatherResponse.sys!!.sunrise * 1000))
                view.findViewById<TextView>(R.id.sunrise).text = sdf.format(date)
                date = Date((weatherResponse.sys!!.sunset * 1000))
                view.findViewById<TextView>(R.id.sunset).text = sdf.format(date)
                loadingView.dismiss()
            }else{
                Log.i("Datos", "No Entro ${response.code()} ${response.errorBody()} ")
            }
        }

        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
            context!!.toast(R.string.errorService)
        }
    })
}

}