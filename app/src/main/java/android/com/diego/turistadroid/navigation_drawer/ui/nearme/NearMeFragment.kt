package android.com.diego.turistadroid.navigation_drawer.ui.nearme

import android.app.AlertDialog
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.Image
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.graphics.*
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import kotlinx.android.synthetic.main.fragment_near_me.*
import java.util.*

class NearMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

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
        anadirLugares()
        initUI()
    }

    private fun anadirLugares(){
        ControllerPlaces.deletePlace(1)
        val fecha = Calendar.getInstance().time
        val img = Image(3, Utilities.bitmapToBase64(BitmapFactory.decodeResource(context!!.resources ,R.drawable.ima_default_place))!!)
        val lugar = Place(1, "nombre", fecha, "ciudad", 4.3,  -3.940906,38.981782)
        lugar.imagenes.add(img)
        ControllerPlaces.insertPlace(lugar)
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
    }

    /**
     * Configuración por defecto del modo de mapa
     */
    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMapToolbarEnabled = true
        // mMap.setMinZoomPreference(12.0f)
    }

    fun puntosEnMapa() {
        // Obtenemos los lugares
        val listaLugares = ControllerPlaces.selectPlaces()
        // Por cada lugar, añadimos su marcador
        // Ademamas vamos a calcular la langitud y la latitud media
        listaLugares?.forEach {
            añadirMarcador(it)
        }
        // Actualiazmos la camara para que los cubra a todos
        actualizarCamara(listaLugares)
        // Añadimos los eventos
        mMap.setOnMarkerClickListener(this)

    }

    /**
     * Actauliza la camara para que lso cubra a todos
     * @param listaLugares MutableList<Lugar>?
     */
    private fun actualizarCamara(listaLugares: MutableList<Place>?) {
        val bc = LatLngBounds.Builder()
        for (item in listaLugares!!) {
            bc.include(LatLng(item.latitud, item.longitud))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 120))
    }

    /**
     * Creamos el marcador
     * @param lugar Lugar
     */
    private fun añadirMarcador(lugar: Place) {
        val posicion = LatLng(lugar.latitud, lugar.longitud)
        val pin: Bitmap = crearPin(lugar.imagenes[0]!!.foto)!!
        val marker = mMap.addMarker(
            MarkerOptions() // Posición
                .position(posicion) // Título
                .title(lugar.nombre) // Subtitulo
                .snippet(lugar.city + " del " + lugar.fecha) // Color o tipo d icono
                .anchor(0.5f, 0.907f)
                .icon(BitmapDescriptorFactory.fromBitmap(pin))
        )
        // Le aádo como tag el lugar para recuperarlo
        marker.tag = lugar
    }

    /**
     * Evento on lck sobre el marcador
     * @param marker Marker
     * @return Boolean
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        val lugar = marker.tag as Place
        Log.i("Mapa", lugar.toString())
        mostrarDialogo(lugar)
        return false
    }

    /**
     * Muestra un dialogo del lugar
     * @param lugar Lugar
     */
    private fun mostrarDialogo(lugar: Place) {
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.intem_visualizacion_mapa, null)
        // Le ponemos las cosas
        val imagen = vista.findViewById(R.id.mapaLugarImagen) as ImageView
        imagen.setImageBitmap(Utilities.base64ToBitmap(lugar.imagenes[0]!!.foto))
        val nombre = vista.findViewById(R.id.mapaLugarTextNombre) as TextView
        nombre.text = lugar.nombre
        val tipo = vista.findViewById(R.id.mapaLugarTextTipo) as TextView
        tipo.text = lugar.city
        val fecha = vista.findViewById(R.id.mapaLugarTextFecha) as TextView
        fecha.text = lugar.fecha.toString()
        builder
            .setView(vista)
            .setIcon(R.drawable.ic_location)
            .setTitle("Lugar")
            // Add action buttons
            .setPositiveButton(R.string.btnSave) { _, _ ->
                null
            }
        //.setNegativeButton(R.string.cancelar, null)
        // setNeutralButton("Maybe", neutralButtonClick)
        builder.show()
    }

    /**
     * Crea un pin personalizado usando la id de la foto
     * @param imagenID String
     * @return Bitmap?
     */
    private fun crearPin(imagenID: String): Bitmap? {
        //val fotografia = lugar.imagenes[0]!!.foto
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f), dp(76f), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.pin_1)
            drawable?.setBounds(0, 0, dp(62f), dp(76f))
            drawable?.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()
            val bitmap = Utilities.base64ToBitmap(imagenID)
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f) / bitmap.width.toFloat()
                matrix.postTranslate(dp(5f).toFloat(), dp(5f).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f).toFloat(), dp(5f).toFloat(), dp(52f + 5).toFloat()] = dp(52f + 5).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f).toFloat(), dp(26f).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (e: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }

    // Densidad de pantalla
    fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else
            Math.ceil((resources.displayMetrics.density * value).toDouble()).toInt()
    }


}