package android.com.diego.turistadroid.navigation_drawer.ui.allplaces

import android.com.diego.turistadroid.MyApplication
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.Places
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.bbdd.firebase.entities.PlaceFB
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlaceDetailFragment
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesViewModel
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_all_places.*
import kotlinx.android.synthetic.main.layout_confirm_delete_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllPlaces : Fragment() {

    // Mis variables
    private var places = mutableListOf<PlaceFB>() // Lista
    private var clickedSort = false
    private lateinit var root: View
    private lateinit var userApi: UserApi

    // Interfaz gráfica
    private lateinit var adapter: MyPlacesViewModel //Adaptador de Recycler
    private var paintSweep = Paint()

    //Modos ordenacion
    private var ascName = true
    private var ascDate = true
    private var ascMark = true

    private lateinit var bbddRest: BBDDRest

    companion object{

        lateinit var myContext: Context
        lateinit var idUser: String

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //val viewSettings = inflater.inflate(R.menu.navigation_drawer, container, false)

        root = inflater.inflate(R.layout.fragment_all_places, container, false)
        userApi = (activity?.application as MyApplication).USUARIO_API
        idUser = userApi.id!!
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Iniciamos la interfaz
        initUI()
    }

    override fun onResume() {
        super.onResume()
        getDatosFromBD()
    }

    private fun initUI() {
        bbddRest = BBDDApi.service
        initFloatingButtons()
        iniciarSwipeRecarga()
        getDatosFromBD()
        // Mostramos las vistas de listas y adaptador asociado
        placeRecycler_AllPlaces.layoutManager = LinearLayoutManager(context)
        orderSites()
    }

    /**
     * Iniciamos el swipe de recarga
     */
    private fun iniciarSwipeRecarga() {
        placeSwipe_AllPlaces.setColorSchemeResources(R.color.colorPrimaryDark)
        placeSwipe_AllPlaces.setProgressBackgroundColorSchemeResource(R.color.colorDialog)
        placeSwipe_AllPlaces.setOnRefreshListener {
            //cargarDatos()
            getDatosFromBD()
        }
    }

    private fun seleccionarImagenesPlace(idPlace: String){

        val call = bbddRest.selectImageByIdLugar(idPlace)

        call.enqueue(object : Callback<List<ImagesDTO>>{
            override fun onResponse(call: Call<List<ImagesDTO>>, response: Response<List<ImagesDTO>>) {
                if (response.isSuccessful) {

                    val listaImagenesDTO = response.body()!!
                    val listaImagenes = ImagesMapper.fromDTO(listaImagenesDTO)

                    for (imagen in listaImagenes){
                        deleteImgPlace(imagen.id!!)
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

    //Eliminamos la imagen del lugar
    private fun deleteImgPlace(id : String) {

        val call = bbddRest.deleteImagesLugar(id)

        call.enqueue(object : Callback<ImagesDTO>{
            override fun onResponse(call: Call<ImagesDTO>, response: Response<ImagesDTO>) {
                if (response.isSuccessful) {
                    Log.i("imagen", "imagenes eliminadas")
                } else {
                    Log.i("imagen", "error al eliminar")
                }
            }
            override fun onFailure(call: Call<ImagesDTO>, t: Throwable) {
                Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Eliminamos el lugar
    private fun deletePlaceBD(place: Places) {

        val call = bbddRest.deletePlace(place.id!!)

        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {
                if (response.isSuccessful) {
                    Log.i("lugar", "lugar eliminado")
                } else {
                    Log.i("lugar", "error al eliminar")
                }
            }
            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                Toast.makeText(context, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })

        seleccionarImagenesPlace(place.id)
    }

    private fun initFloatingButtons() {

        btnSortPlaces_AllPlaces.setOnClickListener {
            onSortButtonClicked()
        }
    }


    //Metodo al hacer click en el FAB ordenar
    private fun onSortButtonClicked() {

        setVisibilitySort(clickedSort)
        setAnimationSort(clickedSort)
        setClickableSort(clickedSort)
        clickedSort = !clickedSort
    }

    /**
     * Iniciamos fragment detalles del lugar
     *
     * @param editable modo edicion
     * @param place Lugar
     * @param pos posicion en el adaptador
     * @param import modo importar
     *
     */
    private fun initDetailsPlaceFragment(editable: Boolean, place: Places, pos: Int?, import: Boolean) {

        val newFragment: Fragment = MyPlaceDetailFragment(editable, place, pos, null, import, userApi)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Visibilidad FABS SORT
    private fun setVisibilitySort(clickedSort: Boolean) {
        if (!clickedSort) {
            btnSortNamePlaceAllPlaces.visibility = View.VISIBLE
            btnSortDatePlaceAllPlaces.visibility = View.VISIBLE
            btnSortMarkPlaceAllPlaces.visibility = View.VISIBLE
        } else {
            btnSortNamePlaceAllPlaces.visibility = View.INVISIBLE
            btnSortDatePlaceAllPlaces.visibility = View.INVISIBLE
            btnSortMarkPlaceAllPlaces.visibility = View.INVISIBLE
        }
    }

    //Animacion FABS Sort
    private fun setAnimationSort(clickedSort: Boolean) {

        //Animaciones
        val fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        val toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)

        if (!clickedSort) {
            btnSortNamePlaceAllPlaces.startAnimation(fromBottom)
            btnSortDatePlaceAllPlaces.startAnimation(fromBottom)
            btnSortMarkPlaceAllPlaces.startAnimation(fromBottom)

        } else {
            btnSortNamePlaceAllPlaces.startAnimation(toBottom)
            btnSortDatePlaceAllPlaces.startAnimation(toBottom)
            btnSortMarkPlaceAllPlaces.startAnimation(toBottom)
        }
    }

    //Clickables FABS SORt
    private fun setClickableSort(clickedSort: Boolean) {
        if (!clickedSort) {
            btnSortNamePlaceAllPlaces.isClickable = true
            btnSortDatePlaceAllPlaces.isClickable = true
            btnSortMarkPlaceAllPlaces.isClickable = true
        } else {
            btnSortNamePlaceAllPlaces.isClickable = false
            btnSortDatePlaceAllPlaces.isClickable = false
            btnSortMarkPlaceAllPlaces.isClickable = false
        }
    }

    //Metodo que ordena sitios y modifica iconos de los FABS
    private fun orderSites() {
/*
        btnSortNamePlaceAllPlaces.setOnClickListener { // Order by NAME
            ascName = if (ascName){
                btnSortNamePlaceAllPlaces.setImageResource(R.drawable.ic_short_name_desc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_name_asc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    lugar1.name!!.toLowerCase().compareTo(lugar2.name!!.toLowerCase()) }
                false
            }else{
                btnSortNamePlaceAllPlaces.setImageResource(R.drawable.ic_short_name_asc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_name_desc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    lugar2.name!!.toLowerCase().compareTo(lugar1.name!!.toLowerCase()) }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortDatePlaceAllPlaces.setOnClickListener { // Order by DATE
            ascDate = if (ascDate){
                btnSortDatePlaceAllPlaces.setImageResource(R.drawable.ic_short_date_desc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_date_asc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha1!!.compareTo(fecha2!!) }
                false
            }else{
                btnSortDatePlaceAllPlaces.setImageResource(R.drawable.ic_short_date_asc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_date_desc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha2!!.compareTo(fecha1!!) }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortMarkPlaceAllPlaces.setOnClickListener { // Order by RATINGS
            ascMark = if (ascMark) {
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlaceAllPlaces.setImageResource(R.drawable.ic_short_mark_desc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    lugar2.votos!!.size.compareTo(lugar1.votos!!.size) }
                false
            }else{
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlaceAllPlaces.setImageResource(R.drawable.ic_short_mark_desc_btn)
                this.places.sortWith { lugar1: Places, lugar2: Places ->
                    lugar1.votos!!.size.compareTo(lugar2.votos!!.size) }
                true
            }
            adapter.notifyDataSetChanged()
        }

 */
    }

    private fun getDatosFromBD() {
        placeSwipe_AllPlaces.isRefreshing = true
        // Seleccionamos los lugares

    }

    /**
     * cargamos los lugares y notificamos
     */
    private fun cargarLugares() {
        adapter = MyPlacesViewModel(places) {
            //eventoClicFila(it)
        }
        placeRecycler_AllPlaces.adapter = adapter
        // Avismos que ha cambiado
        adapter.notifyDataSetChanged()
        placeRecycler_AllPlaces.setHasFixedSize(true)
        placeSwipe_AllPlaces.isRefreshing = false
    }

    /**
     * Evento click asociado a una fila
     * @param place Places
     */
    private fun eventoClicFila(place: Places) {
        initDetailsPlaceFragment(false, place, null, false)
    }
}