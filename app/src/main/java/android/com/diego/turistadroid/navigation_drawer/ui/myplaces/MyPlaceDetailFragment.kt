package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

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
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.HttpClient
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.factorias.FactoriaSliderView
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.splash.SplashScreenActivity
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.generateQRCode
import android.com.diego.turistadroid.utilities.Utilities.toast
import android.com.diego.turistadroid.utilities.slider.SliderImageItem
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.smarteist.autoimageslider.SliderView
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_my_place_detail.*
import kotlinx.android.synthetic.main.layout_change_name_place.view.*
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
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyPlaceDetailFragment(

    private var editable: Boolean,
    private var lugar: Places,
    private var indexPlace: Int? = null,
    private var fragmentAnterior: MyPlacesFragment? = null,
    private var import : Boolean = false,
    private var userApi: UserApi

) : Fragment(), OnMapReadyCallback {

    //Componentes Interfaz
    private lateinit var btnSave : Button
    private lateinit var btnImport : Button
    private lateinit var floatBtnMore : FloatingActionButton
    private lateinit var floatBtnShare : FloatingActionButton
    private lateinit var floatBtnQr : FloatingActionButton
    private lateinit var floatBtnTwitter : FloatingActionButton
    private lateinit var floatBtnEmail : FloatingActionButton
    private lateinit var floatBtnInsta : FloatingActionButton
    private lateinit var sliderView : SliderView

    private var clicked = false //FLoating Button More clicked
    private var clickedShare = false //FLoating Button Share clicked
    private var moreOrShareClick = false
    private lateinit var mMap : GoogleMap //Mapa Google Maps
    private lateinit var location : LatLng //Localizacion
    private lateinit var tarea: CityAsyncTask
    private lateinit var btm: Bitmap

    //Actualizar Lugar
    private var newNamePlace = ""

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var foto: Uri? = null
    private val IMAGEN_PREFIJO = "lugar"
    private val IMAGEN_EXTENSION = ".jpg"
    private val IMAGEN_DIRECTORY = "/TuristaDroid"

    private lateinit var bbddRest: BBDDRest
    private lateinit var clientImgur: OkHttpClient
    private var bases64 = mutableListOf<String>() //Lista donde se almacenan las nuevas imagenes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_my_place_detail, container, false)
        initUI(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        FactoriaSliderView.initSliderView(view, context!!)
    }

    //Metodos del fragment
    private fun init(){
        initClients()
        initMapa()
        initMode()
        initChangeName()
        showDetailsPlace()
        updatePlace()
        sharePlaceOnSocialNetwork()
        onClickQRBtn()
        onImportButton()
        abrirOpciones()
    }

    //clientes para las conexiones con las API de las que consumimos datos
    private fun initClients() {

        clientImgur = HttpClient.getClient()!!
        bbddRest = BBDDApi.service

    }

    /**
     * Mostramos los detalles del lugar en el layout
     */
    private fun showDetailsPlace(){
        txtTitlePlace_DetailsPlace.text = this.lugar.name
        txtUbicationPlace_DetailsPlace.text = this.lugar.city
        seleccionarImagenesPlace()
    }

    /**
     * Cargamos las diferentes imagenes en el viewPager
     */
    private fun seleccionarImagenesPlace(){

        val call = bbddRest.selectImageByIdLugar(this.lugar.id!!)

        call.enqueue(object : Callback<List<ImagesDTO>> {
            override fun onResponse(call: Call<List<ImagesDTO>>, response: Response<List<ImagesDTO>>) {
                if (response.isSuccessful) {

                    val listaImagenesDTO = response.body()!!
                    val listaImagenes = ImagesMapper.fromDTO(listaImagenesDTO)

                    for (imagen in listaImagenes){
                        val sliderItem = SliderImageItem()
                        sliderItem.imageUrl = imagen.url!!
                        FactoriaSliderView.adapterSlider!!.addItem(sliderItem)
                    }

                } else {
                    Log.i("imagen", "error al seleccionar")
                }
            }
            override fun onFailure(call: Call<List<ImagesDTO>>, t: Throwable) {
                Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Detectamos los diferentes componentes del layaout
     */
    private fun initUI(view: View){
        sliderView = view.findViewById(R.id.imageSlider)
        btnSave = view.findViewById(R.id.btnSave_DetailsPlace)
        btnImport = view.findViewById(R.id.btnImport_DetailsPlace)
        floatBtnMore = view.findViewById(R.id.btnFloatShowShare_DetailsPlaces)
        floatBtnShare = view.findViewById(R.id.btnFloatSharePlace_DetailsPlace)
        floatBtnQr = view.findViewById(R.id.btnFloatQRPlace_DetailsPlace)
        floatBtnTwitter = view.findViewById(R.id.btnFloatShareTwitter_DetailsPlace)
        floatBtnEmail = view.findViewById(R.id.btnFloatShareEmail_DetailsPlace)
        floatBtnInsta = view.findViewById(R.id.btnFloatShareInstagram_DetailsPlace)

    }

    /**
     * Segun como hayamos entrado en el fragment los mostrara
     * con unos componentes u otros
     */
    private fun initMode(){

        when {
            editable -> { //Modo Editar Lugar

                btnSave.visibility = View.VISIBLE
                floatBtnMore.isClickable = false
                txtTitlePlace_DetailsPlace.isClickable = true
                //viewPager2.isClickable = true
                notClickable()

            }
            import -> { //Modo Importar Lugar

                btnImport.visibility = View.VISIBLE
                floatBtnMore.isClickable = true
                txtTitlePlace_DetailsPlace.isClickable = false
                //viewPager2.isClickable = false
                floatBtnMore.visibility = View.VISIBLE
                initFloatingButtons()

            }
            else -> { //Modo Visualizar Lugar
                floatBtnMore.visibility = View.VISIBLE
                floatBtnMore.isClickable = true
                //viewPager2.isClickable = false
                initFloatingButtons()
            }
        }
    }

    /**
     * Iniciamos el Mapa
     */
    private fun initMapa() {
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.mapPlace_DetailsPlace) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    //Inicia funcionalidad del boton mostrar mas botones y compartir en redes
    private fun initFloatingButtons(){
        floatBtnMore.setOnClickListener {
            moreOrShareClick = true
            onMoreButtonClicked()
        }

        floatBtnShare.setOnClickListener {
            moreOrShareClick = false
            onMoreButtonClicked()
        }
    }

    //Para poder recuperar el nuevo nombre del lugar creado en el dialog
    private fun setNewName(name: String){
        this.newNamePlace = name
        txtTitlePlace_DetailsPlace.text = name
    }

    //Creacion del dialog cambiar nombre al hacer clic en el TextView
    private fun initChangeName(){
        txtTitlePlace_DetailsPlace.setOnClickListener {
            val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.layout_change_name_place, null)
            val mBuilder = AlertDialog.Builder(context!!)
                .setView(mDialogView).create()
            mBuilder.show()

            //Listener para confirmar el cambio de nombre
            mDialogView.btnConfirmNamePlace_EditPlace.setOnClickListener {
                setNewName(mDialogView.txtNewNamePlace_EditPlace.text.toString())
                mBuilder.dismiss()
            }

            //Listener para cancelar el cambio de nombre
            mDialogView.btnCancelNamePlace_EditPlace.setOnClickListener {
                mBuilder.dismiss()
            }
        }
    }

    //metodo que actualiza un sitio al hacer click en el boton guardar
    private fun updatePlace(){
        btnSave.setOnClickListener {
            if (txtTitlePlace_DetailsPlace.text.isNotEmpty()){//Comprobamos que tenga un nombre

                //Comprobamos que la ubicacion sea valida
                if (txtUbicationPlace_DetailsPlace.equals(getString(R.string.notFoundUbication))){
                    Toast.makeText(context, getString(R.string.ubicationError), Toast.LENGTH_SHORT).show()
                }else {

                    val namePlace = txtTitlePlace_DetailsPlace.text.toString()
                    val city = txtUbicationPlace_DetailsPlace.text.toString()
                    val place = if (this::location.isInitialized){
                        Places( //Nuevo lugar en caso de nueva localizacion
                            this.lugar.id,
                            this.userApi.id,
                            namePlace,
                            this.lugar.fecha,
                            location.latitude.toString(),
                            location.longitude.toString(),
                            this.lugar.votos,
                            city
                        )
                    }else{
                        Places(//Nuevo lugar en caso de no poner nueva localizacion
                            this.lugar.id,
                            this.userApi.id,
                            namePlace,
                            this.lugar.fecha,
                            this.lugar.latitude,
                            this.lugar.longitude,
                            this.lugar.votos,
                            this.lugar.city
                        )
                    }
                    actualizarLugar(place) //Actaulizamos
                    this.fragmentAnterior?.actualizarPlaceAdapter(place, this.indexPlace!!)//Actualizamos el adaptador
                    if (bases64.size > 0){
                        recorrerListBase64()
                    }
                    initMyPlacesFragment()//Volvemos al anterior fragment
                }

            }else{
                //Campos vacios
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()

            }
        }
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
                        lugar.id,
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

    private fun actualizarLugar(place: Places){

        val lugarDTO = PlacesMapper.toDTO(place)
        val call = bbddRest.updatePlace(place.id!!, lugarDTO)

        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {
                if (response.isSuccessful){
                    context!!.toast(R.string.updatePlace)
                }else{
                    context!!.toast(R.string.errorUpdatePlace)
                }
            }

            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                context!!.toast(R.string.errorService)
            }
        })

    }

    //inciar fragment Mis Lugares
    private fun initMyPlacesFragment(){

        val newFragment: Fragment = MyPlacesFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.commit()

    }

    //Al hacer click en el boton importar insertamos un nuevo lugar en la BD
    private fun onImportButton(){
        btnImport.setOnClickListener {
            val currentDate = Utilities.dateToString(Utilities.getSysDate()) //Fecha actual
            val place = Places(this.lugar.id, this.userApi.id, this.lugar.name, currentDate,
                this.lugar.latitude, this.lugar.longitude, "0", this.lugar.city)

            val call = bbddRest.selectPlaceById(this.lugar.id!!)

            call.enqueue(object : Callback<PlacesDTO>{
                override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {

                    if (response.isSuccessful){
                        if (response.body() != null){
                            Toast.makeText(context, getString(R.string.placeExsistente), Toast.LENGTH_SHORT).show()
                        }else{
                            insertarLugar(place)
                        }
                    }
                }
                override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                    Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
                }
            })
            this.fragmentAnterior?.insertarPlaceAdapter(place)//Actualizamos adapter
            initMyPlacesFragment()
        }
    }

    private fun insertarLugar(place: Places){
        val placeDTO = PlacesMapper.toDTO(place)

        val call = bbddRest.insertPlace(placeDTO)

        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {
               if (response.isSuccessful){
                   Log.i("lugar", "insertado con exito")
               }else{
                   Log.i("lugar", "error al insertar")
               }
            }
            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Metedos que se activan segun el FAB que hayamos pulsado
    private fun onMoreButtonClicked() {
        if (moreOrShareClick){
            setVisibility(clicked)
            setAnimation(clicked)
            setClickable(clicked)
            clicked = !clicked
        }else{
            setVisibility(clickedShare)
            setAnimation(clickedShare)
            setClickable(clickedShare)
            clickedShare = !clickedShare
        }

    }

    //Metodo que activa la funcion de clickable de un FAB
    private fun setClickable(clicked: Boolean) {

        if (moreOrShareClick){//Pulsado boton Mostrar Mas
            if (!clicked) {
                floatBtnShare.isClickable = true
                floatBtnQr.isClickable = true
            } else {
                floatBtnShare.isClickable = false
                floatBtnQr.isClickable = false
            }
        }else{//Pulsado boton Compartir
            if (!clickedShare) {
                floatBtnMore.isClickable = false
                floatBtnTwitter.isClickable = true
                floatBtnEmail.isClickable = true
                floatBtnInsta.isClickable = true
            } else {
                floatBtnTwitter.isClickable = false
                floatBtnEmail.isClickable = false
                floatBtnInsta.isClickable = false
                floatBtnMore.isClickable = true
            }
        }
    }

    //metodo que desactiva la funcion de isClickable
    private fun notClickable(){
        floatBtnShare.isClickable = false
        floatBtnQr.isClickable = false
        floatBtnTwitter.isClickable = false
        floatBtnEmail.isClickable = false
        floatBtnInsta.isClickable = false
    }

    //Modificamos la visibilidad
    private fun setVisibility(clicked: Boolean) {
        if (moreOrShareClick) {
            if (!clicked) {
                floatBtnShare.visibility = View.VISIBLE
                floatBtnQr.visibility = View.VISIBLE
                txtSharePlace_Details.visibility = View.VISIBLE
                txtQRPlace_Details.visibility = View.VISIBLE
            } else {
                floatBtnShare.visibility = View.INVISIBLE
                floatBtnQr.visibility = View.INVISIBLE
                txtSharePlace_Details.visibility = View.INVISIBLE
                txtQRPlace_Details.visibility = View.INVISIBLE
            }
        }else{
            if (!clickedShare) {
                floatBtnTwitter.visibility = View.VISIBLE
                floatBtnInsta.visibility = View.VISIBLE
                floatBtnEmail.visibility = View.VISIBLE
                txtShareTwitter_Details.visibility = View.VISIBLE
                txtShareInstagram_Details.visibility = View.VISIBLE
                txtShareEmail_Details.visibility = View.VISIBLE
            } else {
                floatBtnTwitter.visibility = View.INVISIBLE
                floatBtnInsta.visibility = View.INVISIBLE
                floatBtnEmail.visibility = View.INVISIBLE
                txtShareTwitter_Details.visibility = View.INVISIBLE
                txtShareInstagram_Details.visibility = View.INVISIBLE
                txtShareEmail_Details.visibility = View.INVISIBLE
            }
        }
    }

    //Añadimos animaciones
    private fun setAnimation(clicked: Boolean) {

        //Animaciones
        val rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_detail_place_anim)
        val rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_detail_place_anim)
        val fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_detail_place_anim)
        val toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_detail_place_anim)
        //AnimacionesShare
        val fromBottomShare = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        val toBottomShare = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)

        if (moreOrShareClick and !clickedShare){
            if (!clicked) {
                floatBtnShare.startAnimation(fromBottom)
                floatBtnQr.startAnimation(fromBottom)
                txtSharePlace_Details.startAnimation(fromBottom)
                txtQRPlace_Details.startAnimation(fromBottom)
                floatBtnMore.startAnimation(rotateOpen)

            } else {
                floatBtnShare.startAnimation(toBottom)
                floatBtnQr.startAnimation(toBottom)
                txtSharePlace_Details.startAnimation(toBottom)
                txtQRPlace_Details.startAnimation(toBottom)
                floatBtnMore.startAnimation(rotateClose)
            }
        }else{
            if (!clicked) {
                floatBtnTwitter.startAnimation(fromBottomShare)
                floatBtnInsta.startAnimation(fromBottomShare)
                floatBtnEmail.startAnimation(fromBottomShare)
                txtShareTwitter_Details.startAnimation(fromBottomShare)
                txtShareInstagram_Details.startAnimation(fromBottomShare)
                txtShareEmail_Details.startAnimation(fromBottomShare)

            } else {
                floatBtnTwitter.startAnimation(toBottomShare)
                floatBtnInsta.startAnimation(toBottomShare)
                floatBtnEmail.startAnimation(toBottomShare)
                txtShareTwitter_Details.startAnimation(toBottomShare)
                txtShareInstagram_Details.startAnimation(toBottomShare)
                txtShareEmail_Details.startAnimation(toBottomShare)
            }
        }
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
        )
        val markerOptions = MarkerOptions().position(location).icon(icon)
        mMap.addMarker(markerOptions)

    }

    //Al hacer click en el boton del QR
    private fun onClickQRBtn(){
        floatBtnQr.setOnClickListener {
            shareOnQR()
        }
    }

    //MEtodo que crea un QR
    private fun shareOnQR(){
        val builder = AlertDialog.Builder(context!!)
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.layout_share_qr_code, null)

        //Creamos un lugar sin los votos
        val lugarSinVotos = Places(this.lugar.id, this.userApi.id, this.lugar.name, this.lugar.fecha,
            this.lugar.latitude, this.lugar.longitude, "0", this.lugar.city)

        //Generamos el QR
        val code = generateQRCode(Gson().toJson(lugarSinVotos))
        val qrCodeImageView = vista.findViewById(R.id.imagenCodigoQR) as ImageView
        qrCodeImageView.setImageBitmap(code)
        builder
            .setView(vista)
            .setIcon(R.drawable.ic_qr)
            .setTitle(getString(R.string.shareQr))
            .setPositiveButton(R.string.confirmDelete) { _, _ ->
                compartirQRCode(code)
            }
            .setNegativeButton(R.string.cancelDelete, null)
        builder.show()
    }

    /**
     * Comparte un código QR
     * @param code Bitmap
     */
    private fun compartirQRCode(qrCode: Bitmap) {
        Log.i("QR", "Aceptar QR")
        // Politicas de seguridad
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val nombre = Fotos.crearNombreFoto(IMAGEN_PREFIJO, IMAGEN_EXTENSION)
        val fichero =
            Fotos.copiarFoto(qrCode, nombre, IMAGEN_DIRECTORY, 100, context!!)
        Log.i("QR", "Foto salvada: " + fichero.absolutePath)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fichero))
        }
        context?.startActivity(Intent.createChooser(shareIntent, null))
        Log.i("QR", "Foto salvada")
    }

    //Compartir un lugar en redes sociales, mediante intent,  segun la que pulse
    private fun sharePlaceOnSocialNetwork(){
        //Twitter
        floatBtnTwitter.setOnClickListener {

            val img = FactoriaSliderView.adapterSlider!!.getItem().image!!
            val uri = Utilities.getImageUri(context!!, img)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.twitter.android")
            try {
                startActivity(intent)
            }catch (e : ActivityNotFoundException){
                Toast.makeText(context, getString(R.string.fatalTwitter), Toast.LENGTH_SHORT).show()
            }
        }

        //Instagram
        floatBtnInsta.setOnClickListener {
            val img = FactoriaSliderView.adapterSlider!!.getItem().image!!
            val uri = Utilities.getImageUri(context!!, img)
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setPackage("com.instagram.android")
            try {
                startActivity(intent)
            }catch (e : ActivityNotFoundException){
                Toast.makeText(context, getString(R.string.fatalTwitter), Toast.LENGTH_SHORT).show()
            }
        }

        //Email
        floatBtnEmail.setOnClickListener {
            val img = FactoriaSliderView.adapterSlider!!.getItem().image!!
            val uri = Utilities.getImageUri(context!!, img)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:" + userApi.email)}
            intent.putExtra(Intent.EXTRA_SUBJECT, lugar.name)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(intent)

        }
    }

    //Centramos la camara
    private fun moveCamera(){
        val latitud = this.lugar.latitude!!.toDouble()
        val longitud = this.lugar.longitude!!.toDouble()
        val location = LatLng(latitud, longitud)
        if (editable) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
        placeMarker(location)
    }

    /**
    * Configuración  del modo de mapa segun la forma
     * en la que entramos al fragment
    */
    private fun configurarIUMapa(boolean: Boolean) {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = boolean
        uiSettings.isTiltGesturesEnabled = boolean
        uiSettings.isCompassEnabled = boolean
        uiSettings.isZoomControlsEnabled = boolean
        uiSettings.isMapToolbarEnabled = false
        if (editable){
            mMap.setMinZoomPreference(10.0f)
        }else{
            mMap.setMinZoomPreference(15.0f)
        }
    }

    //Permitir crear puntos en caso de ser modo Editable
    private fun initMapaSwitchEditable(){
        if (editable){
            configurarIUMapa(true)
            mMap.setOnMapClickListener { latLng ->

                mMap.clear()
                location = LatLng(latLng.latitude, latLng.longitude)
                placeMarker(location)
                cargarCiudad(latLng.latitude, latLng.longitude)
            }
        }else{
            configurarIUMapa(false)
        }
    }

    //Mapa listo
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        initMapaSwitchEditable()
        moveCamera()
    }

    //Abrimos un dialog con las opciones para abrir camara o galeria
    private fun abrirOpciones() {
        sliderView.setOnClickListener{
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
    private fun abrirCamara() = if (ActivityCompat.checkSelfPermission(
            context!!,
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
                Toast.makeText(context, getString(R.string.errorLogin), Toast.LENGTH_SHORT).show()
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

    //Metodo en el que inicializamos la Tarea para detectar la ciudad
    private fun cargarCiudad(latitude: Double, longiude: Double){

        tarea = CityAsyncTask(latitude, longiude)
        tarea.execute()
    }

    //Clase ASyncrona que detecta segun el marcador la ciudad en la que se encuentra
    inner class CityAsyncTask(latitude: Double, longiude: Double) : AsyncTask<String, String, String>() {

        private var latitud = latitude
        private var longitud = longiude

        override fun doInBackground(vararg params: String?): String {

            var result = ""
            val geocoder = Geocoder(context!!, Locale.getDefault())
            try {
                //Contiene la direccion completa incluido pais, ciudad...
                val addresses : List<Address> = geocoder.getFromLocation(latitud, longitud, 1)
                when {
                    (addresses[0].locality != null) and (addresses[0].countryName != null) -> {
                        txtUbicationPlace_DetailsPlace.text = addresses[0].locality + " - " + addresses[0].countryName
                    }
                    (addresses[0].locality == null) and (addresses[0].countryName != null) -> {
                        txtUbicationPlace_DetailsPlace.text = addresses[0].countryName
                    }
                    else -> {
                        txtUbicationPlace_DetailsPlace.text = getString(R.string.notFoundUbication)
                    }
                }
                result = addresses[0].toString()
            }catch (e: IOException){}
            catch (e: Exception) {}

            return result
        }
    }
}