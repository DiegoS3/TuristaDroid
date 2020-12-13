package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.Manifest
import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerImages
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.Image
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.generateQRCode
import android.com.diego.turistadroid.utilities.slider.SliderAdapter
import android.com.diego.turistadroid.utilities.slider.SliderItem
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
import android.os.Handler
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.zxing.qrcode.encoder.QRCode
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_my_place_detail.*
import kotlinx.android.synthetic.main.layout_change_name_place.view.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException
import java.util.*
import kotlin.math.abs

class MyPlaceDetailFragment(

    private var editable: Boolean,
    private var lugar: Place,
    private var indexPlace: Int? = null,
    private var fragmentAnterior: MyPlacesFragment? = null,
    private var import : Boolean = false

) : Fragment(), OnMapReadyCallback, RatingBar.OnRatingBarChangeListener {

    //Componentes Interfaz
    private lateinit var btnSave : Button
    private lateinit var btnImport : Button
    private lateinit var floatBtnMore : FloatingActionButton
    private lateinit var floatBtnShare : FloatingActionButton
    private lateinit var floatBtnQr : FloatingActionButton
    private lateinit var floatBtnTwitter : FloatingActionButton
    private lateinit var floatBtnEmail : FloatingActionButton
    private lateinit var floatBtnInsta : FloatingActionButton
    private lateinit var ratingBar: RatingBar

    //Componentes Slider Image
    private lateinit var viewPager2 : ViewPager2
    private lateinit var adapter: SliderAdapter
    private var sliderHandler = Handler()
    //Lista de imagenes
    private var sliderItems =  mutableListOf<SliderItem>()
    private var listaImagenes = mutableListOf<Image>()
    private var images = RealmList<Image>()

    //Usuario logeado
    private var user = LogInActivity.user

    private var clicked = false //FLoating Button More clicked
    private var clickedShare = false //FLoating Button Share clicked
    private var moreOrShareClick = false
    private var mark : Float = 0f
    private lateinit var mMap : GoogleMap //Mapa Google Maps
    private lateinit var location : LatLng //Localizacion
    private lateinit var tarea: CityAsyncTask

    //Actualizar Lugar
    private var newNamePlace = ""

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var foto: Uri? = null
    private val IMAGEN_PREFIJO = "lugar"
    private val IMAGEN_EXTENSION = ".jpg"
    private val IMAGEN_DIRECTORY = "/TuristaDroid"

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
    }

    private fun init(){
        initMapa()
        initMode()
        initChangeName()
        initViewPager()
        lugaresUsuario()
        showDetailsPlace()
        abrirOpciones()
        updatePlace()
        sharePlaceOnSocialNetwork()
        onClickQRBtn()
        onImportButton()
    }

    private fun lugaresUsuario(){
        for (item in user.places){
            if (this.lugar.id == item.id){
                mark = item.puntuacion.toFloat()
                Log.i("Puntuacion", mark.toString())
            }else{
                mark = 0f
            }
        }
    }

    private fun showDetailsPlace(){
        txtTitlePlace_DetailsPlace.text = this.lugar.nombre
        txtUbicationPlace_DetailsPlace.text = this.lugar.city
        ratingBar.rating = mark
        imagenesLugar()
    }

    private fun imagenesLugar(){
        for (item in this.lugar.imagenes){
            val img = Utilities.base64ToBitmap(item.foto)!!
            listaImagenes.add(item)
            addSliderItem(img)
        }
    }

    private fun initUI(view: View){

        viewPager2 = view.findViewById(R.id.vpImagesPlace_DetailsPlace)
        btnSave = view.findViewById(R.id.btnSave_DetailsPlace)
        btnImport = view.findViewById(R.id.btnImport_DetailsPlace)
        floatBtnMore = view.findViewById(R.id.btnFloatShowShare_DetailsPlaces)
        floatBtnShare = view.findViewById(R.id.btnFloatSharePlace_DetailsPlace)
        floatBtnQr = view.findViewById(R.id.btnFloatQRPlace_DetailsPlace)
        floatBtnTwitter = view.findViewById(R.id.btnFloatShareTwitter_DetailsPlace)
        floatBtnEmail = view.findViewById(R.id.btnFloatShareEmail_DetailsPlace)
        floatBtnInsta = view.findViewById(R.id.btnFloatShareInstagram_DetailsPlace)
        ratingBar = view.findViewById(R.id.ratingBarPlace_DetailsPlace)
        ratingBar.onRatingBarChangeListener = this

    }

    private fun initMode(){

        when {
            editable -> {

                btnSave.visibility = View.VISIBLE
                floatBtnMore.isClickable = false
                txtTitlePlace_DetailsPlace.isClickable = true
                viewPager2.isClickable = true
                notClickable()

            }
            import -> {

                btnImport.visibility = View.VISIBLE
                floatBtnMore.isClickable = true
                ratingBar.setIsIndicator(true)
                txtTitlePlace_DetailsPlace.isClickable = false
                viewPager2.isClickable = false
                ratingBar.focusable = View.NOT_FOCUSABLE
                floatBtnMore.visibility = View.VISIBLE
                initFloatingButtons()

            }
            else -> {
                ratingBar.setIsIndicator(true)
                ratingBar.focusable = View.NOT_FOCUSABLE
                floatBtnMore.visibility = View.VISIBLE
                floatBtnMore.isClickable = true
                viewPager2.isClickable = false
                initFloatingButtons()
            }
        }
    }

    /**
     * Inicia el Mapa
     */
    private fun initMapa() {
        Log.i("Mapa", "Iniciando Mapa")
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.mapPlace_DetailsPlace) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    private fun initViewPager(){
        adapter = SliderAdapter(sliderItems, viewPager2)
        viewPager2.adapter = adapter
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(20))
        compositePageTransformer.addTransformer { page, position ->

            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f

        }

        viewPager2.setPageTransformer(compositePageTransformer)

        //Metodo para que las imagenes se pasen solas
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })
    }

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

    private fun setNewName(name: String){
        this.newNamePlace = name
        txtTitlePlace_DetailsPlace.text = name
    }

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

    //Obtener puntuacion del sitio
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        mark = rating
    }

    private fun updatePlace(){
        btnSave.setOnClickListener {
            if (txtTitlePlace_DetailsPlace.text.isNotEmpty()){

                if (txtUbicationPlace_DetailsPlace.equals(getString(R.string.notFoundUbication))){
                    Toast.makeText(context, getString(R.string.ubicationError), Toast.LENGTH_SHORT).show()
                }else {

                    val namePlace = txtTitlePlace_DetailsPlace.text.toString()
                    val city = txtUbicationPlace_DetailsPlace.text.toString()
                    val place = if (this::location.isInitialized){
                        Place(
                            this.lugar.id,
                            namePlace,
                            this.lugar.fecha,
                            city,
                            mark.toDouble(),
                            location.longitude,
                            location.latitude
                        )
                    }else{
                        Place(
                            this.lugar.id,
                            namePlace,
                            this.lugar.fecha,
                            city,
                            mark.toDouble(),
                            this.lugar.longitud,
                            this.lugar.latitud
                        )
                    }
                    addImagePlace(images, place)
                    ControllerPlaces.updatePlace(place)
                    this.fragmentAnterior?.actualizarPlaceAdapter(place, this.indexPlace!!)
                    initMyPlacesFragment()
                }

            }else{

                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun initMyPlacesFragment(){

        val newFragment: Fragment = MyPlacesFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.commit()

    }

    private fun onImportButton(){
        btnImport.setOnClickListener {
            val currentDate = Calendar.getInstance().time
            val id = ControllerPlaces.getPlaceIdentity()
            val place = Place(id, this.lugar.nombre, currentDate, this.lugar.city, this.lugar.puntuacion, this.lugar.longitud, this.lugar.latitud)
            addImagePlaceImport(this.lugar)
            ControllerPlaces.insertPlace(place)
            this.fragmentAnterior?.insertarPlaceAdapter(place)
            initMyPlacesFragment()
        }
    }

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

    private fun setClickable(clicked: Boolean) {

        if (moreOrShareClick){
            if (!clicked) {
                floatBtnShare.isClickable = true
                floatBtnQr.isClickable = true
            } else {
                floatBtnShare.isClickable = false
                floatBtnQr.isClickable = false
            }
        }else{
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

    private fun notClickable(){
        floatBtnShare.isClickable = false
        floatBtnQr.isClickable = false
        floatBtnTwitter.isClickable = false
        floatBtnEmail.isClickable = false
        floatBtnInsta.isClickable = false
    }

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

    private fun addSliderItem(bitmap: Bitmap){
        val image = SliderItem(bitmap)
        sliderItems.add(image)
    }

    private var sliderRunnable = Runnable {

        run {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }
    }

    private fun addImageBd(bitmap: Bitmap){
        val imgStr = Utilities.bitmapToBase64(bitmap)!!
        val id = ControllerImages.getImageIdentity()
        val img = Image(id, imgStr)
        images.add(img)
        ControllerImages.insertImage(img)
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

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

    private fun onClickQRBtn(){
        floatBtnQr.setOnClickListener {
            shareOnQR()
        }
    }

    private fun shareOnQR(){
        val builder = AlertDialog.Builder(context!!)
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.layout_share_qr_code, null)

        val lugarSinImagenes = Place(lugar.id, lugar.nombre, lugar.fecha, lugar.city, lugar.puntuacion, lugar.longitud, lugar.latitud)

        val code = generateQRCode(Gson().toJson(lugarSinImagenes))
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

    private fun sharePlaceOnSocialNetwork(){
        floatBtnTwitter.setOnClickListener {

            val img = Utilities.base64ToBitmap(this.lugar.imagenes[0]!!.foto)
            val uri = Utilities.getImageUri(context!!, img!!)
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

        floatBtnInsta.setOnClickListener {
            val img = Utilities.base64ToBitmap(this.lugar.imagenes[0]!!.foto)
            val uri = Utilities.getImageUri(context!!, img!!)
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

        floatBtnEmail.setOnClickListener {
            val img = Utilities.base64ToBitmap(this.lugar.imagenes[0]!!.foto)
            val uri = Utilities.getImageUri(context!!, img!!)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:" + user.email)}
            intent.putExtra(Intent.EXTRA_SUBJECT, lugar.nombre)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(intent)

        }
    }

    private fun addImagePlaceImport(place: Place){

        for (imgPlace in this.lugar.imagenes){
            val img = Utilities.base64ToBitmap(imgPlace.foto)!!
            addImageBd(img)
            place.imagenes.add(imgPlace)}

    }


    private fun addImagePlace(list: MutableList<Image>, place: Place){

        for (imgPlace in this.lugar.imagenes){ place.imagenes.add(imgPlace)}
        for (imagen in list){ place.imagenes.add(imagen) }
    }

    private fun moveCamera(){
        val latitud = this.lugar.latitud
        val longitud = this.lugar.longitud
        val location = LatLng(latitud, longitud)
        if (editable) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
        placeMarker(location)
    }

    /**
    * Configuración por defecto del modo de mapa
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        initMapaSwitchEditable()
        moveCamera()

    }

    private fun abrirOpciones() {
        viewPager2.setOnClickListener(){
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



            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, foto)
            addSliderItem(bitmap)
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

    private fun cargarCiudad(latitude: Double, longiude: Double){

        tarea = CityAsyncTask(latitude, longiude)
        tarea.execute()
    }

    inner class CityAsyncTask(latitude: Double, longiude: Double) : AsyncTask<String, String, String>() {

        private var latitud = latitude
        private var longitud = longiude

        override fun doInBackground(vararg params: String?): String {

            var result = ""
            val geocoder = Geocoder(context!!, Locale.getDefault())
            try {
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