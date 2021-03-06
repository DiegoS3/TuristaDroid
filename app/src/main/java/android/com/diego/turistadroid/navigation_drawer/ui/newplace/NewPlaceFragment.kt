package android.com.diego.turistadroid.navigation_drawer.ui.newplace


import android.Manifest.permission.*
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
import android.com.diego.turistadroid.navigation_drawer.ui.maps.MapsFragment
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
import android.text.Editable
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
import com.google.android.gms.maps.model.LatLng
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_newplace.*
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


class NewPlaceFragment(
    private var userApi: UserApi
) : Fragment() {

    private lateinit var tarea: CityAsyncTask
    private lateinit var txtUbication : EditText
    private lateinit var btnAddImage : Button

    private lateinit var location : LatLng
    private lateinit var idLugar : String
    private var bases64 = mutableListOf<String>()
    private lateinit var bbddRest: BBDDRest
    private lateinit var clientImgur: OkHttpClient

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var foto: Uri? = null

    //Variables de OnSave/OnRestore
    private var namePlace = ""
    private var listaImg = arrayListOf<String>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_newplace, container, false)

        FactoriaSliderView.initSliderView(root, activity!!)//Iniciamos el Slider de las Imagenes


        txtUbication = root.findViewById(R.id.txtUbicationPlace_NewPlace)
        btnAddImage = root.findViewById(R.id.btnAddImage_NewPlace)


        // Inflate the layout for this fragment
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (MapsFragment.maps){
            this.location = MapsFragment.location
            Log.i("LocationMap", location.toString())
            cargarCiudad(location.latitude, location.longitude)
        }
    }

    private fun init(){
        initClients()
        createPlace()
        abrirOpciones()
        initMapsFragment()
    }

    //clientes para las conexiones con las API de las que consumimos datos
    private fun initClients() {
        clientImgur = HttpClient.getClient()!!
        bbddRest = BBDDApi.service
    }

    private fun abrirOpciones() {
        btnAddImage.setOnClickListener(){
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
    private fun abrirCamara() = if (ActivityCompat.checkSelfPermission(context!!, CAMERA) == PackageManager.PERMISSION_DENIED ||
        ActivityCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        val permisosCamara =
            arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
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
        val permiso = arrayOf(READ_EXTERNAL_STORAGE)
        requestPermissions(permiso, GALERIA)
    }
    //muetsro la galeria
    private fun mostrarGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALERIA)
    }

    private fun initMyPlacesFragment(){

        val newFragment: Fragment = MyPlacesFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initMapsFragment(){

        btnOpenMap_NewPlace.setOnClickListener {
            val newFragment: Fragment = MapsFragment(userApi)
            val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun createPlace(){

        btnSave_NewPlace.setOnClickListener {

            if (comprobarVacios()){

                if (txtUbicationPlace_NewPlace.text.toString() == getString(R.string.notFoundUbication)){
                    Toast.makeText(context, getString(R.string.ubicationError), Toast.LENGTH_SHORT).show()
                }else {
                    val currentDate = Utilities.dateToString(Utilities.getSysDate()) //Fecha actual
                    val namePlace = txtNamePlace_NewPlace.text.toString()
                    idLugar = UUID.randomUUID().toString()
                    val city = txtUbication.text.toString()
                    val listaVotos = arrayListOf<String>()
                    val place = Places(idLugar, userApi.id, namePlace, currentDate, location.latitude.toString(), location.longitude.toString(), listaVotos, city)
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

        call.enqueue(object : Callback<PlacesDTO> {
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

    private fun comprobarVacios() : Boolean{
        return txtNamePlace_NewPlace.text.isNotEmpty() and txtUbicationPlace_NewPlace.text.isNotEmpty()
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
                else
                    Toast.makeText(
                        context,
                        "No tienes permiso para acceder a la galería",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            CAMARA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    abrirCamara()
                else
                    Toast.makeText(context, "No tienes permiso para acceder a la cámara", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    // Para salvar el estado por ejemplo es usando un Bundle en el ciclo de vida
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("NAME", namePlace)
            putStringArrayList("IMAGENES", listaImg)
        }
        // Siempre se llama a la superclase para salvar las cosas
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Recuperamos del Bundle
        savedInstanceState?.run {
            namePlace = getString("NAME").toString()
            listaImg = getStringArrayList("IMAGENES")!!

        }
    }


    private fun cargarCiudad(latitude: Double, longiude : Double){

        tarea = CityAsyncTask(latitude, longiude)
        tarea.execute()
    }

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