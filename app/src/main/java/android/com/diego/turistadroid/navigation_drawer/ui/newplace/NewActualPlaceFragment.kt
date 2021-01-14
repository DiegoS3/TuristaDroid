package android.com.diego.turistadroid.navigation_drawer.ui.newplace

import android.Manifest
import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.*
import android.com.diego.turistadroid.factorias.FactoriaSliderView
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.slider.SliderImageItem
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.*
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_new_actual_place.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException
import java.util.*


class NewActualPlaceFragment : Fragment(), RatingBar.OnRatingBarChangeListener {

    private lateinit var tarea: CityAsyncTask
    private lateinit var txtUbication : EditText
    private lateinit var ratingBar : RatingBar
    private lateinit var btnAddImage : Button
    private var mark : Double = 0.0

    //Lista de imagenes
    private var images = RealmList<Image>()

    //Usuario logeado
    private var user = LogInActivity.user

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var foto: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_new_actual_place, container, false)

        FactoriaSliderView.initSliderView(root, activity!!)//Iniciamos el Slider de las Imagenes

        txtUbication = root.findViewById(R.id.txtUbicationPlace_NewActualPlace)
        ratingBar = root.findViewById(R.id.ratingBarPlace_NewActualPlace)
        btnAddImage = root.findViewById(R.id.btnAddImage_NewActualPlace)
        ratingBar.onRatingBarChangeListener = this

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init(){
        getCurrentLocation()
        abrirOpciones()
    }

    //Dialos para la camara o galeria
    private fun abrirOpciones() {
        btnAddImage.setOnClickListener{
            val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(context!!)
                .setView(mDialogView).create()
            mBuilder.show()

            //Listener para abrir la camara
            mDialogView.txtCamara.setOnClickListener {
                abrirCamara()
                mBuilder.dismiss()
            }

            //Listener para abrir la galeria
            mDialogView.txtGaleria.setOnClickListener {
                abrirGaleria()
                mBuilder.dismiss()
            }
        }
    }

    //muestro la camara
    private fun abrirCamara() = if (ActivityCompat.checkSelfPermission(context!!,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_DENIED ||
        ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        val permisosCamara =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permisosCamara, CAMARA)
    }else{
        mostrarCamara()
    }

    //abre la camara y hace la foto
    private fun mostrarCamara(){
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        foto = context?.contentResolver!!.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, foto)
        startActivityForResult(intent, CAMARA)
    }

    //pido los permisos para abrir la galeria
    private fun abrirGaleria(){
        val permiso = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissions(permiso, GALERIA)
    }
    //muetsro la galeria
    private fun mostrarGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALERIA)
    }

    //Añado imagen a la BD
    private fun addImageBd(bitmap: Bitmap){
        val imgStr = Utilities.bitmapToBase64(bitmap)!!
        val id = ControllerImages.getImageIdentity()
        val img = Image(id, imgStr)
        images.add(img)
        ControllerImages.insertImage(img)
    }

    ////Añado imagen al RealmList de lugar
    private fun addImagePlace(list: MutableList<Image>, place: Place){

        for (imagen in list){ place.imagenes.add(imagen) }

    }

    //Inicio Fragment mis lugares
    private fun initMyPlacesFragment(){

        val newFragment: Fragment = MyPlacesFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    //Creamos un lugar al hacer click en el boton guardar y tras comprobar las
    //diferentes condiciones que se deben de dar
    private fun createPlace(latitude: Double, longitude : Double){

        btnSave_NewActualPlace.setOnClickListener {

            if (txtNamePlace_NewActualPlace.text.isNotEmpty()){
                if (txtUbicationPlace_NewActualPlace.text.toString() == (getString(R.string.notFoundUbication))){
                    Toast.makeText(context, getString(R.string.ubicationError), Toast.LENGTH_SHORT).show()
                }else {
                    val currentDate = Calendar.getInstance().time
                    val namePlace = txtNamePlace_NewActualPlace.text.toString()
                    val city = txtUbication.text.toString()
                    val id = ControllerPlaces.getPlaceIdentity()
                    val place = Place(id, namePlace, currentDate, city, mark, longitude, latitude)
                    ControllerPlaces.insertPlace(place)
                    addImagePlace(images, place)
                    user.places.add(place)
                    //Creamos un usuario nuevo con los datos del logeado y le incluimos los lugares
                    val newUser = User(user.email, user.nombre, user.nombreUser, user.pwd, user.foto, user.places)
                    ControllerUser.updateUser(newUser)
                    Utilities.vibratePhone(context)
                    initMyPlacesFragment()
                }
            }else{
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //si el usuario selecciona una imagen, la almacenamos en la lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode==GALERIA) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, data?.data)
                val sliderItem = SliderImageItem()
                sliderItem.description = "Slider Item Added Manually"
                //.imageUrl =
                //"https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
                sliderItem.image = bitmap
                FactoriaSliderView.adapterSlider!!.addItem(sliderItem)

                addImageBd(bitmap)


            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, foto)

            val sliderItem = SliderImageItem()
            sliderItem.description = "Slider Item Added Manually"
            //sliderItem.imageUrl =
            //"https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
            sliderItem.image = bitmap
            FactoriaSliderView.adapterSlider!!.addItem(sliderItem)
            addImageBd(bitmap)
        }
    }

    //obtengo el resultado de pedir los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            GALERIA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mostrarGaleria()
            }
            CAMARA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    abrirCamara()
            }
        }
    }

    //Obtener puntuacion del sitio
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        mark = rating.toDouble()
    }

    //Posicon actual usuario
    private fun getCurrentLocation(){

        val locationRequest = LocationRequest()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 200
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(activity!!)
            .requestLocationUpdates(locationRequest, object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(activity!!).removeLocationUpdates(this)

                    if (locationResult.locations.size > 0){
                        //Obtenemos la ultima posicion conocida
                        val latestLocationIndex = locationResult.locations.size - 1
                        val latitude = locationResult.locations[latestLocationIndex].latitude //LATITUD
                        val longitude = locationResult.locations[latestLocationIndex].longitude //LONGITUD

                        cargarCiudad(latitude, longitude)
                        createPlace(latitude, longitude)
                    }
                }
            }, Looper.getMainLooper())
    }

    //Metodo en el que inicializamos la Tarea para detectar la ciudad
    private fun cargarCiudad(latitude: Double, longiude : Double){

        tarea = CityAsyncTask(latitude, longiude)
        tarea.execute()
    }

    //Clase ASyncrona que detecta segun el marcador la ciudad en la que se encuentra
    inner class CityAsyncTask(latitude: Double, longiude : Double) : AsyncTask<String, String, String>() {

        private var latitud = latitude
        private var longitud = longiude

        override fun doInBackground(vararg params: String?): String {

            var result = ""
            val geocoder = Geocoder(context!!, Locale.getDefault())
            try {
                val addresses : List<Address> = geocoder.getFromLocation(latitud, longitud, 1)
                when {
                    (addresses[0].locality != null) and (addresses[0].countryName != null) -> {
                        txtUbication.setText(addresses[0].locality + " - " + addresses[0].countryName)
                    }
                    (addresses[0].locality == null) and (addresses[0].countryName != null) -> {
                        txtUbication.setText(addresses[0].countryName)
                    }
                    else -> {
                        txtUbication.setText(getString(R.string.notFoundUbication))
                    }
                }
                result = addresses[0].toString()
            }catch (e : IOException){}
            catch (e : Exception) {}

            return result
        }
    }
}

