package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.slider.SliderAdapter
import android.com.diego.turistadroid.utilities.slider.SliderItem
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_my_place_detail.*
import kotlinx.android.synthetic.main.fragment_myplaces.*
import kotlinx.android.synthetic.main.item_list_places.*
import kotlin.math.abs

class MyPlaceDetailFragment(

    private var editable : Boolean,
    private var lugar : Place

    ) : Fragment(), OnMapReadyCallback {

    //Componentes Interfaz
    private lateinit var btnSave : Button
    private lateinit var floatBtnMore : FloatingActionButton
    private lateinit var floatBtnShare : FloatingActionButton
    private lateinit var floatBtnQr : FloatingActionButton
    private lateinit var floatBtnGoTo : FloatingActionButton
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

    //Usuario logeado
    private var user = LogInActivity.user

    private var clicked = false //FLoating Button More clicked
    private var clickedShare = false //FLoating Button Share clicked
    private var moreOrShareClick = false
    private var mark : Float = 0f
    private lateinit var mMap : GoogleMap //Mapa Google Maps

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
        initViewPager()
        lugaresUsuario()
        showDetailsPlace()
    }

    private fun lugaresUsuario(){
        for (item in user.places){
            if (this.lugar.id == item.id){
                mark = item.puntuacion.toFloat()
                Log.i("Puntuacion", mark.toString())
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
            addSliderItem(img)
        }
    }

    private fun initUI(view : View){

        viewPager2 = view.findViewById(R.id.vpImagesPlace_DetailsPlace)
        btnSave = view.findViewById(R.id.btnSave_DetailsPlace)
        floatBtnMore = view.findViewById(R.id.btnFloatShowShare_DetailsPlaces)
        floatBtnShare = view.findViewById(R.id.btnFloatSharePlace_DetailsPlace)
        floatBtnQr = view.findViewById(R.id.btnFloatQRPlace_DetailsPlace)
        floatBtnGoTo = view.findViewById(R.id.btnFloatGoToPlace_DetailsPlace)
        floatBtnTwitter = view.findViewById(R.id.btnFloatShareTwitter_DetailsPlace)
        floatBtnEmail = view.findViewById(R.id.btnFloatShareEmail_DetailsPlace)
        floatBtnInsta = view.findViewById(R.id.btnFloatShareInstagram_DetailsPlace)
        ratingBar = view.findViewById(R.id.ratingBarPlace_DetailsPlace)

    }

    private fun initMode(){

        if (editable){

            btnSave.visibility = View.VISIBLE
            floatBtnMore.isClickable = false
            notClickable()

        }else{
            ratingBar.setIsIndicator(true)
            ratingBar.focusable = View.NOT_FOCUSABLE
            floatBtnMore.visibility = View.VISIBLE
            floatBtnMore.isClickable = true
            initFloatingButtons()
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
                floatBtnGoTo.isClickable = true
            } else {
                floatBtnShare.isClickable = false
                floatBtnQr.isClickable = false
                floatBtnGoTo.isClickable = false
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
        floatBtnGoTo.isClickable = false
        floatBtnTwitter.isClickable = false
        floatBtnEmail.isClickable = false
        floatBtnInsta.isClickable = false
    }

    private fun setVisibility(clicked: Boolean) {
        if (moreOrShareClick) {
            if (!clicked) {
                floatBtnShare.visibility = View.VISIBLE
                floatBtnGoTo.visibility = View.VISIBLE
                floatBtnQr.visibility = View.VISIBLE
                txtSharePlace_Details.visibility = View.VISIBLE
                txtGoToPlace_Details.visibility = View.VISIBLE
                txtQRPlace_Details.visibility = View.VISIBLE
            } else {
                floatBtnShare.visibility = View.INVISIBLE
                floatBtnGoTo.visibility = View.INVISIBLE
                floatBtnQr.visibility = View.INVISIBLE
                txtSharePlace_Details.visibility = View.INVISIBLE
                txtGoToPlace_Details.visibility = View.INVISIBLE
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
                floatBtnGoTo.startAnimation(fromBottom)
                floatBtnQr.startAnimation(fromBottom)
                txtSharePlace_Details.startAnimation(fromBottom)
                txtGoToPlace_Details.startAnimation(fromBottom)
                txtQRPlace_Details.startAnimation(fromBottom)
                floatBtnMore.startAnimation(rotateOpen)

            } else {
                floatBtnShare.startAnimation(toBottom)
                floatBtnGoTo.startAnimation(toBottom)
                floatBtnQr.startAnimation(toBottom)
                txtSharePlace_Details.startAnimation(toBottom)
                txtGoToPlace_Details.startAnimation(toBottom)
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

    private fun moveCamera(){
        val latitud = this.lugar.latitud
        val longitud = this.lugar.longitud
        val location = LatLng(latitud, longitud)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        placeMarker(location)
    }

    /**
    * Configuración por defecto del modo de mapa
    */
    private fun configurarIUMapa(boolean : Boolean) {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = boolean
        uiSettings.isTiltGesturesEnabled = boolean
        uiSettings.isCompassEnabled = boolean
        uiSettings.isZoomControlsEnabled = boolean
        uiSettings.isMapToolbarEnabled = boolean
        mMap.setMinZoomPreference(15.0f)
    }

    private fun initMapaSwitchEditable(){
        if (editable){
            configurarIUMapa(true)
        }else{
            configurarIUMapa(false)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        initMapaSwitchEditable()
        moveCamera()
    }
}