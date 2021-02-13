package android.com.diego.turistadroid.navigation_drawer.ui.maps

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewPlaceFragment
import android.graphics.*
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_newplace.*
import java.util.*

class MapsFragment(
    private var userFB: FirebaseUser
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //Mis variables
    private lateinit var mMap: GoogleMap

    //Variables que usaremos en otras actividades
    companion object{
        lateinit var location: LatLng
        var maps = false
    }

    //Creacion de la vista
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    //Con la vista creada activamos el evento que detecta clicks en el mapa
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { v, event ->
            return@setOnTouchListener true
        }
        initUI()
        getCurrentLocation()//Obtenemos posicion actual
    }

    /**
     * Iniciamos diferentes metodos
     */
    private fun initUI() {
        initMapa()
        saveLocation()
    }

    /**
     * Metodo que obteien la posicon actual del usuario
     */
    private fun getCurrentLocation(){
        val locationRequest = LocationRequest()
        locationRequest.interval = 2000 //Intervalo con el que se actualiza
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(activity!!)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val latestLocationIndex = locationResult.locations.size - 1 //Ultima localizacion conocida
                    var latitud = locationResult.locations[latestLocationIndex].latitude //LATITUD
                    var longitud = locationResult.locations[latestLocationIndex].longitude //LONGITUD
                    val currentLocation = LatLng(latitud, longitud)
                    moverCamara(currentLocation)//centramos la camara en la nueva posicion
                }
            }, Looper.getMainLooper())
    }

    /**
     * Iniciamos el Mapa
     */
    private fun initMapa() {
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.miMapa) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    /**
     * Metodo que detecta que el mapa esta listo
     *
     * @param googleMap GoogleMap
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->

            mMap.clear()//limpiamos
            location = LatLng(latLng.latitude, latLng.longitude)//nueva localizacion
            placeMarker(location)//creamos marcador
        }

        configurarIUMapa()//configuracion del mapa
    }

    /**
     * Metodo que centra la camara/pantalla en la posicion que
     * le pasamos como parametro
     *
     *@param latLng posicion del usuario
     *
     */
    private fun moverCamara(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    /**
     * Configuracion que le damos al mapa
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL //Tipo de mapa
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true //Permitir gestos
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true //brujula
        uiSettings.isZoomControlsEnabled = true //permitir zoom
        uiSettings.isMapToolbarEnabled = false //barra de ir a y abrir googlemaps
        mMap.setMinZoomPreference(5.0f)//zoom minimo
    }

    /**
     * Metodo que crea un nuevo pin
     *
     * @param location Localizacion en la que pincha/seEncuentra el usuario
     *
     */
    private fun placeMarker(location: LatLng){
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.pin_centrado_2
            )
        )//Icono personalizado del pin
        val markerOptions = MarkerOptions().position(location).icon(icon)//creamos el marcador custom
        mMap.addMarker(markerOptions)//lo añadimos
    }

    /**
     * evento al hacer click en el marcador
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    /**
     * Metodo que iniciar el Fragment Crear un Nuevo Lugar
     */
    private fun initNewPlaceFragment() {

        val newFragment: Fragment = NewPlaceFragment(userFB)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        //transaction.addToBackStack(null)
        transaction.commit()

    }

    /**
     * Guardamos la nueva localizacion e iniciamos el Fragment
     * Nuevo Lugar
     */
    private fun saveLocation(){
        btnSelectLocation_NewPlace.setOnClickListener {
             maps = true
             initNewPlaceFragment()
        }
    }
}