package android.com.diego.turistadroid.navigation_drawer.ui.newplace

import android.Manifest
import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.*
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.Images
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.Places
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.Votes
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.HttpClient
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.factorias.FactoriaSliderView
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.toast
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
import android.util.Log
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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*


class NewActualPlaceFragment(
    private var userApi: UserApi
) : Fragment() {

    private lateinit var tarea: CityAsyncTask
    private lateinit var txtUbication : EditText
    private lateinit var btnAddImage : Button

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var foto: Uri? = null

    private lateinit var bbddRest: BBDDRest
    private lateinit var clientImgur: OkHttpClient
    private lateinit var idLugar : String
    private var bases64 = mutableListOf<String>() //Lista donde se almacenan las nuevas imagenes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_new_actual_place, container, false)

        FactoriaSliderView.initSliderView(root, activity!!)//Iniciamos el Slider de las Imagenes

        txtUbication = root.findViewById(R.id.txtUbicationPlace_NewActualPlace)
        btnAddImage = root.findViewById(R.id.btnAddImage_NewActualPlace)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init(){
        initClients()
        getCurrentLocation()
        abrirOpciones()
    }

    //clientes para las conexiones con las API de las que consumimos datos
    private fun initClients() {
        clientImgur = HttpClient.getClient()!!
        bbddRest = BBDDApi.service
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
                    val currentDate = Utilities.dateToString(Utilities.getSysDate()) //Fecha actual
                    val namePlace = txtNamePlace_NewActualPlace.text.toString()
                    val city = txtUbication.text.toString()
                    idLugar = UUID.randomUUID().toString()
                    val listaVotos = arrayListOf<String>()
                    val place = Places(idLugar, userApi.id, namePlace, currentDate, latitude.toString(), longitude.toString(), listaVotos, city)
                    insertNewPlace(place)
                    //insertPlaceVotes(idLugar)
                    if (bases64.size > 0){
                        recorrerListBase64()
                    }
                    Utilities.vibratePhone(context)
                    initMyPlacesFragment()
                }
            }else{
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Insertamos un nuevo lugar en la BD de la API
    private fun insertNewPlace(place: Places) {
        val dto = PlacesMapper.toDTO(place)
        val call = bbddRest.insertPlace(dto)

        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {
                if (response.isSuccessful){
                    context!!.toast(R.string.placeSignUp)
                }else{
                    context!!.toast(R.string.errorUpdatePlace)
                }
            }
            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                context!!.toast(R.string.errorService)
            }
        })
    }

    //Metodo que recorrer la lista de bases64
    private fun recorrerListBase64(){
        for (i in bases64){
            uploadImgToImgurAPI(i)
        }
    }

    /**
     * Subimos la imagen que ha elegido el usuario a la
     * Api de IMGUR y creamos el usuario que posteriormente
     * registramos en la bbdd de nuestra API
     */
    private fun uploadImgToImgurAPI(imaString : String) {
        //loadingView.show()

        val mediaType: MediaType = "text/plain".toMediaTypeOrNull()!!
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", imaString)
            .build()
        val request = ImgurREST.postImage(body, "base64")
        Log.i("answer", request.toString() + " " + request.body.toString())
        clientImgur.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Toast.makeText(context, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (response.isSuccessful) {

                    val data = JSONObject(response.body!!.string())
                    val item = data.getJSONObject("data")
                    val image = Images(
                        UUID.randomUUID().toString(),
                        idLugar,
                        item.getString("link")
                    )
                    insertImageApi(image)

                } else {
                    Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Registramos el usuario mediante retrofit en la
     * bbdd de nuestra API
     */
    private fun insertImageApi(image: Images) {
        val dto = ImagesMapper.toDTO(image)
        val call = bbddRest.insertImage(dto)

        call.enqueue((object : Callback<ImagesDTO> {
            override fun onResponse(call: Call<ImagesDTO>, response: Response<ImagesDTO>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {

                    Log.i("image", "imagen subida")

                } else {
                    Log.i("image", "error subir imagen")
                }
            }

            //Si error
            override fun onFailure(call: Call<ImagesDTO>, t: Throwable) {
                Toast.makeText(
                    context,
                    getString(R.string.userNoSignUp),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }))
    }

    //si el usuario selecciona una imagen, la almacenamos en la lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode==GALERIA) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, data?.data)
                val sliderItem = SliderImageItem()
                sliderItem.image = bitmap
                FactoriaSliderView.adapterSlider!!.addItem(sliderItem)
                val base64 = Utilities.bitmapToBase64(bitmap)!!
                bases64.add(base64)


            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, foto)
            val sliderItem = SliderImageItem()
            sliderItem.image = bitmap
            FactoriaSliderView.adapterSlider!!.addItem(sliderItem)

            val base64 = Utilities.bitmapToBase64(bitmap)!!
            bases64.add(base64)
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

