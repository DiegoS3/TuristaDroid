package android.com.diego.turistadroid.navigation_drawer.ui.nearme

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlaceDetailFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.graphics.*
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_near_me.*
import java.util.*


class NearMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private var primera = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_near_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { v, event ->
            return@setOnTouchListener true
        }

        getCurrentLocation()
        initUI()
    }

    //Recuperamos localizacion actual del usuario
    private fun getCurrentLocation(){
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
                            mMap.clear()
                            positionMarker(currentLocation)
                            marcadoresLugares(currentLocation)
                            moverCamara(currentLocation)
                            Log.i("currentLocation", "$latitud, $longitud")
                            //Toast.makeText(context, "Location update: "+latitud.toString() + ", " + longitud.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }, Looper.getMainLooper())
    }

    //Movemos camara a la posicion
    private fun moverCamara(latLng: LatLng){
        if(primera){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            primera = false
        }
    }

    private fun initUI() {
        miMapaProgressBar.visibility = View.VISIBLE
        initMapa()
        miMapaProgressBar.visibility = View.GONE
    }

    /**
     * Iniciamos el Mapa
     */
    private fun initMapa() {
        Log.i("Mapa", "Iniciando Mapa")
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.miMapa) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    /**
     * EL mapa está listo
     * @param googleMap GoogleMap
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        puntosEnMapa()
        clickInfoWindow()
        mMap.setOnMapClickListener { latLng ->
            //mMap.clear()
            Log.i("Mapa", "Pulsado")
            val location = LatLng(latLng.latitude, latLng.longitude)
            //placeMarker(location)
            Log.i("Mapa", location.longitude.toString() + " " + location.latitude.toString())
        }
    }

    //Crear Marcador del usuario
    private fun positionMarker(location: LatLng){
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.ic_location
            )
        )
        val markerOptions = MarkerOptions().position(location).icon(icon)
        Log.i("placeMarker pintado:", location.latitude.toString() + ", " + location.longitude.toString())
        mMap.addMarker(markerOptions)
    }

    //Creamos pin del usuario
    private fun placeMarker(location: LatLng, place: Place){
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.pin_centrado_2
            )
        )
        val markerOptions = MarkerOptions().position(location).icon(icon).snippet("Prueba")
        Log.i("placeMarker pintado:", location.latitude.toString() + ", " + location.longitude.toString())
        val a = mMap.addMarker(markerOptions)
        a.tag = place
    }


    /**
     * Configuración por defecto del modo de mapa
     */
    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMapToolbarEnabled = false
        mMap.setMinZoomPreference(15.0f)
    }

    private fun puntosEnMapa() {
        mMap.setOnMarkerClickListener(this)
    }

    private fun marcadoresLugares(latLng: LatLng) {
        val listaPlaces  = ControllerPlaces.selectNearby(latLng.latitude, latLng.longitude)!!
        //var listaPlaces  = ControllerPlaces.selectPlaces()!!
        var i = 0
        listaPlaces.forEach { _ ->
            val location = LatLng(listaPlaces[i].latitud, listaPlaces[i].longitud)
            placeMarker(location, listaPlaces[i])
            i++
        }
    }

    /**
     * Evento on click sobre el marcador
     * @param marker Marker
     * @return Boolean
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        Log.i("Pulsado", "sadfs")
        val lugar = marker.tag as Place
        Log.i("Mapa", lugar.nombre)
        infoWindow()
        return false
    }

    //Modificamos el infoWindow que se vera al hacer click en el marcador
    private fun infoWindow(){
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter{
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val row: View = layoutInflater.inflate(R.layout.info_place_near_me, null)
                val txtNamePlaceInfo: TextView = row.findViewById(R.id.namePlace_infoWindow)
                val imaPlaceInfo: ImageView = row.findViewById(R.id.imaPlace_infoWindow)
                val place =  marker.tag as Place
                txtNamePlaceInfo.text = place.nombre
                imaPlaceInfo.setImageBitmap(Utilities.base64ToBitmap(place.imagenes[0]!!.foto))
                return row
            }
        })
    }

    //Al hacer click en el infoWindow abrimos el lugar para ver sus detalles
    private fun clickInfoWindow(){
        mMap.setOnInfoWindowClickListener {
            val place =  it.tag as Place
            Toast.makeText(context,  "Marker: "+place.nombre , Toast.LENGTH_SHORT).show()
            abrirMyPlacesDetail(place)
        }
    }

    //Abrimos el fragment de lugar detalles sin modo edicion
    private fun abrirMyPlacesDetail(lugar: Place){
        val editable = false
        val newFragment: Fragment = MyPlaceDetailFragment(editable, lugar)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}