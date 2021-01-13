package android.com.diego.turistadroid.navigation_drawer.ui.newplace


import android.Manifest.permission.*
import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.*
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.maps.MapsFragment
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.slider.SliderAdapter
import android.com.diego.turistadroid.utilities.slider.SliderAdapterExample
import android.com.diego.turistadroid.utilities.slider.SliderImageItem
import android.com.diego.turistadroid.utilities.slider.SliderItem
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.model.LatLng
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.fragment_my_place_detail.*
import kotlinx.android.synthetic.main.fragment_newplace.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException
import java.util.*
import kotlin.math.abs


class NewPlaceFragment () : Fragment(), RatingBar.OnRatingBarChangeListener {

    private lateinit var tarea: CityAsyncTask
    //private lateinit var viewPager2 : ViewPager2
    private lateinit var txtUbication : EditText
    private lateinit var ratingBar : RatingBar
    private lateinit var btnAddImage : Button
    //private lateinit var adapter: SliderAdapter
    private var adapterSlider: SliderAdapterExample? = null
    private var mark : Double = 0.0
    private var sliderHandler = Handler()
    //Lista de imagenes
    private var sliderItems =  mutableListOf<SliderItem>()
    private var images = RealmList<Image>()
    private lateinit var location : LatLng

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

        val root = inflater.inflate(R.layout.fragment_newplace, container, false)

        val sliderView = root.findViewById<SliderView>(R.id.imageSlider)
        adapterSlider = SliderAdapterExample(activity!!)
        sliderView.setSliderAdapter(adapterSlider!!)
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        sliderView.indicatorSelectedColor = Color.WHITE
        sliderView.indicatorUnselectedColor = Color.GRAY
        sliderView.scrollTimeInSec = 3
        sliderView.isAutoCycle = true
        sliderView.startAutoCycle()
        sliderView.setOnIndicatorClickListener {
            Log.i(
                "GGG",
                "onIndicatorClicked: " + sliderView.currentPagePosition
            )
        }

        txtUbication = root.findViewById(R.id.txtUbicationPlace_NewPlace)
        //viewPager2 = root.findViewById(R.id.vpImagesPlace_NewPlace)
        ratingBar = root.findViewById(R.id.ratingBarPlace_NewPlace)
        btnAddImage = root.findViewById(R.id.btnAddImage_NewPlace)
        ratingBar.onRatingBarChangeListener = this


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
        createPlace()
        abrirOpciones()
        initMapsFragment()
        initViewPager()
        //prueba()
        //prueba()
    }

    private fun prueba(){
        val sliderItem = SliderImageItem()
        sliderItem.description = "Slider Item Added Manually"
        sliderItem.imageUrl = "PRUEBA"
        //"https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        //sliderItem.image = Uri.parse("android.resource://com.diego.turistadroid/" + R.drawable.ima_default_place)
        adapterSlider!!.addItem(sliderItem)
    }

    //Inciamos el adaptador del slider
    private fun initViewPager(){
        //adapter = SliderAdapter(sliderItems, viewPager2)
        //viewPager2.adapter = adapter
        //viewPager2.clipToPadding = false
        //viewPager2.clipChildren = false
        //viewPager2.offscreenPageLimit = 3
        //viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(20))
        compositePageTransformer.addTransformer { page, position ->

            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f

        }

        //viewPager2.setPageTransformer(compositePageTransformer)

        //Metodo para que las imagenes se pasen solas
        /*viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }

        })*/
    }

    /*private var sliderRunnable = Runnable {

        run {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }
    }*/

    override fun onPause() {
        super.onPause()
        //sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        //sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    //Obtener puntuacion del sitio
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
       mark = rating.toDouble()
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

    private fun addSliderItem(bitmap: Bitmap){
        val image = SliderItem(bitmap)
        addImageBd(bitmap)
        sliderItems.add(image)
    }

    private fun addImageBd(bitmap: Bitmap){
        val imgStr = Utilities.bitmapToBase64(bitmap)!!
        val id = ControllerImages.getImageIdentity()
        val img = Image(id, imgStr)
        images.add(img)
        ControllerImages.insertImage(img)
    }

    private fun addImagePlace(list: MutableList<Image>, place: Place){

        for (imagen in list){ place.imagenes.add(imagen) }

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
            val newFragment: Fragment = MapsFragment()
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
                    val currentDate = Calendar.getInstance().time
                    val namePlace = txtNamePlace_NewPlace.text.toString()
                    val id = ControllerPlaces.getPlaceIdentity()
                    val city = txtUbication.text.toString()
                    val place = Place(id, namePlace, currentDate, city, mark, location.longitude, location.latitude)
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

    private fun comprobarVacios() : Boolean{
        return txtNamePlace_NewPlace.text.isNotEmpty() and txtUbicationPlace_NewPlace.text.isNotEmpty()
    }

    //si el usuario selecciona una imagen, la almacenamos en la lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode==GALERIA) {
            try {
                val sliderItem = SliderImageItem()
                sliderItem.description = "Slider Item Added Manually"
                //.imageUrl =
                    //"https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
                sliderItem.image = data?.data
                adapterSlider!!.addItem(sliderItem)


            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            //val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, foto)
            //foto?.let { addSliderItem(it) }
            //addSliderItem(bitmap)
            val sliderItem = SliderImageItem()
            sliderItem.description = "Slider Item Added Manually"
            //sliderItem.imageUrl =
                //"https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
            sliderItem.image = foto
            adapterSlider!!.addItem(sliderItem)
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