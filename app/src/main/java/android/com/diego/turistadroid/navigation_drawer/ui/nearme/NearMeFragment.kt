package android.com.diego.turistadroid.navigation_drawer.ui.nearme

import android.app.AlertDialog
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerBbdd
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.Image
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlaceDetailFragment
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
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
import androidx.core.content.ContextCompat
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
    private lateinit var locationCallback: LocationCallback
    private var primera = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_near_me, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { v, event ->
            return@setOnTouchListener true
        }

        //requireActivity().actionBar!!.title = getString(R.string.near_me)

        getCurrentLocation()
        anadirLugares()
        initUI()
        

    }


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
                            var latitud = locationResult.locations[latestLocationIndex].latitude //LATITUD
                            var longitud = locationResult.locations[latestLocationIndex].longitude //LONGITUD
                            val currentLocation = LatLng(latitud, longitud)
                            mMap.clear()
                            positionMarker(currentLocation)
                            marcadoresLugares(currentLocation)
                            moverCamara(currentLocation)
                            Log.i("currentLocation", latitud.toString() + ", " + longitud.toString())
                            //Toast.makeText(context, "Location update: "+latitud.toString() + ", " + longitud.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }, Looper.getMainLooper())
    }

    private fun moverCamara(latLng: LatLng){
        if(primera){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            primera = false
        }
    }


    private fun anadirLugares(){
        ControllerPlaces.deleteAllPlaces()
        ControllerBbdd.deleteAllPlaces()
        val fecha = Calendar.getInstance().time
        val img = Image(
            1, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img2 = Image(
            2, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img3 = Image(
            3, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img4 = Image(
            4, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img5 = Image(
            5, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img6 = Image(
            6, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img7 = Image(
            7, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img8 = Image(
            8, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )
        val img9 = Image(
            9, Utilities.bitmapToBase64(
                BitmapFactory.decodeResource(
                    context!!.resources,
                    R.drawable.ima_default_place
                )
            )!!
        )

        //Ciudad Real
        val lugar = Place(1, "Lugar 1", fecha, "ciudad", 4.3, -3.940100, 38.981782)
        var lugar2 = Place(2, "Lugar 2", fecha, "ciudad", 3.5, -3.945000, 38.981000)
        var lugar3 = Place(3, "Lugar 3", fecha, "ciudad", 0.5, -3.940900, 38.981500)

        val lugar7 = Place(7, "Lugar 1", fecha, "ciudad", 4.3, -3.918120, 38.980935)
        var lugar8 = Place(8, "Lugar 2", fecha, "ciudad", 3.5, -3.940031, 38.993067)
        var lugar9 = Place(9, "Lugar 3", fecha, "ciudad", 0.5, -3.942874, 38.971091)

        //Puertollano
        val lugar4 = Place(4, "Lugar 1", fecha, "ciudad", 4.3, -4.11179038, 38.707595)
        var lugar5 = Place(5, "Lugar 2", fecha, "ciudad", 3.5, -4.11017800, 38.702733)
        var lugar6 = Place(6, "Lugar 3", fecha, "ciudad", 0.5, -4.08364100, 38.682322)


        lugar.imagenes.add(img)
        lugar2.imagenes.add(img2)
        lugar3.imagenes.add(img3)
        lugar4.imagenes.add(img4)
        lugar5.imagenes.add(img5)
        lugar6.imagenes.add(img6)
        lugar7.imagenes.add(img7)
        lugar8.imagenes.add(img8)
        lugar9.imagenes.add(img9)

        ControllerPlaces.insertPlace(lugar)
        ControllerPlaces.insertPlace(lugar2)
        ControllerPlaces.insertPlace(lugar3)
        ControllerPlaces.insertPlace(lugar4)
        ControllerPlaces.insertPlace(lugar5)
        ControllerPlaces.insertPlace(lugar6)
        ControllerPlaces.insertPlace(lugar7)
        ControllerPlaces.insertPlace(lugar8)
        ControllerPlaces.insertPlace(lugar9)
    }


    private fun initUI() {
        miMapaProgressBar.visibility = View.VISIBLE
        initMapa()
        miMapaProgressBar.visibility = View.GONE
    }

    /**
     * Inicia el Mapa
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
        //pintarPosicionActual()
        //marcadoresLugares()
        Log.i("pintada posicion actual", "pintado")
        mMap.setOnMapClickListener { latLng ->
            //mMap.clear()
            Log.i("Mapa", "Pulsado")
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            val location = LatLng(latLng.latitude, latLng.longitude)
            //placeMarker(location)
            Log.i("Mapa", location.longitude.toString() + " " + location.latitude.toString())
        }
    }

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

    fun puntosEnMapa() {
        mMap.setOnMarkerClickListener(this)

    }

    private fun marcadoresLugares(latLng: LatLng) {
        var listaPlaces  = ControllerPlaces.selectNearby(latLng.latitude, latLng.longitude)!!
        //var listaPlaces  = ControllerPlaces.selectPlaces()!!
        var i = 0
        listaPlaces.forEach { _ ->
            var location = LatLng(listaPlaces[i].latitud, listaPlaces[i].longitud)
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
        //mostrarDialogo(lugar)
        infoWindow()
        return false
    }


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

    private fun clickInfoWindow(){
        mMap.setOnInfoWindowClickListener {
            val place =  it.tag as Place
            Toast.makeText(context,  "Marker: "+place.nombre , Toast.LENGTH_SHORT).show()
            abrirMyPlacesDetail(place)
        }
    }

    private fun abrirMyPlacesDetail(lugar: Place){
        var editable = false
        val newFragment: Fragment = MyPlaceDetailFragment(editable, lugar)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}