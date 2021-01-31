package android.com.diego.turistadroid.navigation_drawer.ui.nearme

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.Places
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlaceDetailFragment
import android.com.diego.turistadroid.utilities.Utilities.toast
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
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_near_me.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class NearMeFragment(
    private val userApi: UserApi
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private var primera = true
    private lateinit var bbddRest: BBDDRest

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
        bbddRest = BBDDApi.service
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
                            marcadoresLugares()
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
    private fun placeMarker(location: LatLng, place: Places){
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

    private fun marcadoresLugares() {
        val call = bbddRest.selectAllPlaces()
        call.enqueue(object : Callback<List<PlacesDTO>>{
            override fun onResponse(call: Call<List<PlacesDTO>>, response: Response<List<PlacesDTO>>) {
                if (response.isSuccessful){
                    val listaPlacesDTO = response.body()!!
                    val listaPlaces  = PlacesMapper.fromDTO(listaPlacesDTO)
                    //var listaPlaces  = ControllerPlaces.selectPlaces()!!
                    var i = 0
                    listaPlaces.forEach { _ ->
                        val location = LatLng(listaPlaces[i].latitude!!.toDouble(), listaPlaces[i].longitude!!.toDouble())
                        placeMarker(location, listaPlaces[i])
                        i++
                    }
                }
            }

            override fun onFailure(call: Call<List<PlacesDTO>>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    /**
     * Evento on click sobre el marcador
     * @param marker Marker
     * @return Boolean
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        Log.i("Pulsado", "sadfs")
        val lugar = marker.tag as Places
        Log.i("Mapa", lugar.name!!)
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
                val place =  marker.tag as Places
                txtNamePlaceInfo.text = place.name
                cargarImagenInfoWindow(place.id, imaPlaceInfo)
                return row
            }
        })
    }

    private fun cargarImagenInfoWindow(id: String?, imaPlaceInfo: ImageView) {
        val call = bbddRest.selectImageByIdLugar(id!!)

        call.enqueue(object : Callback<List<ImagesDTO>>{
            override fun onResponse(call: Call<List<ImagesDTO>>, response: Response<List<ImagesDTO>>) {
                if (response.isSuccessful){
                    val listaImagenesDTO = response.body()!!
                    val listaImagenes = ImagesMapper.fromDTO(listaImagenesDTO)
                    if (listaImagenes.isNotEmpty()){
                        Glide.with(context!!)
                            .load(listaImagenes[0])
                            .fitCenter()
                            .into(imaPlaceInfo)
                    }else{
                        context!!.toast(R.string.errorUpload)
                    }
                }
            }

            override fun onFailure(call: Call<List<ImagesDTO>>, t: Throwable) {
                context!!.toast(R.string.errorService)
            }
        })
    }

    //Al hacer click en el infoWindow abrimos el lugar para ver sus detalles
    private fun clickInfoWindow(){
        mMap.setOnInfoWindowClickListener {
            val place =  it.tag as Places
            Toast.makeText(context,  "Marker: "+place.name , Toast.LENGTH_SHORT).show()
            abrirMyPlacesDetail(place)
        }
    }

    //Abrimos el fragment de lugar detalles sin modo edicion
    private fun abrirMyPlacesDetail(lugar: Places){
        val editable = false
        val newFragment: Fragment = MyPlaceDetailFragment(editable, lugar, null, null, false, userApi)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}