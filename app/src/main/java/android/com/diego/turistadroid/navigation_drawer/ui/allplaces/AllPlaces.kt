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
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.com.diego.turistadroid.bbdd.firebase.mappers.Mappers
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlaceDetailFragment
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_all_places.*
import kotlinx.android.synthetic.main.fragment_myplaces.*
import kotlinx.android.synthetic.main.layout_confirm_delete_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllPlaces : Fragment() {

    // Mis variables
    private var places = mutableListOf<PlaceFB>() // Lista
    private var clickedSort = false
    private lateinit var root: View
    private lateinit var userFB: FirebaseUser

    // Interfaz gráfica
    private lateinit var adapter: MyPlacesViewModel //Adaptador de Recycler
    private var paintSweep = Paint()

    //Modos ordenacion
    private var ascName = true
    private var ascDate = true
    private var ascMark = true

    //Vars Firebase
    private lateinit var FireStore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storage_ref: StorageReference

    companion object {

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
        userFB = (activity?.application as MyApplication).USUARIO_FIRE
        idUser = userFB.uid
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Iniciamos la interfaz
        initUI()
    }

    private fun initUI() {
        initFirebase()
        initFloatingButtons()
        iniciarSwipeRecarga()
        // Mostramos las vistas de listas y adaptador asociado
        placeRecycler_AllPlaces.layoutManager = LinearLayoutManager(context)
        cargarLugares()
        orderSites()
    }

    private fun initFirebase() {
        storage = Firebase.storage("gs://turistadroid.appspot.com/")
        storage_ref = storage.reference
        FireStore = FirebaseFirestore.getInstance()
    }

    /**
     * Iniciamos el swipe de recarga
     */
    private fun iniciarSwipeRecarga() {
        placeSwipe_AllPlaces.setColorSchemeResources(R.color.colorPrimaryDark)
        placeSwipe_AllPlaces.setProgressBackgroundColorSchemeResource(R.color.colorDialog)
        placeSwipe_AllPlaces.setOnRefreshListener {
            //cargarDatos()
            cargarLugares()
        }
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
        /*
        val newFragment: Fragment = MyPlaceDetailFragment(editable, place, pos, null, import, userFB)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()

         */
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

        btnSortNamePlaceAllPlaces.setOnClickListener { // Order by NAME
            ascName = if (ascName) {
                btnSortNamePlaceAllPlaces.setImageResource(R.drawable.ic_short_name_desc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_name_asc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar1.name!!.toLowerCase().compareTo(lugar2.name!!.toLowerCase())
                }
                false
            } else {
                btnSortNamePlaceAllPlaces.setImageResource(R.drawable.ic_short_name_asc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_name_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar2.name!!.toLowerCase().compareTo(lugar1.name!!.toLowerCase())
                }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortDatePlaceAllPlaces.setOnClickListener { // Order by DATE
            ascDate = if (ascDate) {
                btnSortDatePlaceAllPlaces.setImageResource(R.drawable.ic_short_date_desc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_date_asc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha1!!.compareTo(fecha2!!)
                }
                false
            } else {
                btnSortDatePlaceAllPlaces.setImageResource(R.drawable.ic_short_date_asc_btn)
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_date_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha2!!.compareTo(fecha1!!)
                }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortMarkPlaceAllPlaces.setOnClickListener { // Order by RATINGS
            ascMark = if (ascMark) {
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlaceAllPlaces.setImageResource(R.drawable.ic_short_mark_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar2.votos!!.toInt().compareTo(lugar1.votos!!.toInt())
                }
                false
            } else {
                btnSortPlaces_AllPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlaceAllPlaces.setImageResource(R.drawable.ic_short_mark_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar1.votos!!.toInt().compareTo(lugar2.votos!!.toInt())
                }
                true
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun getDatosFromBD() {

        // Seleccionamos los lugares
        FireStore.collection("places")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error al acceder al servicio: " + e.localizedMessage,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@addSnapshotListener
                }
                //placeSwipe_MyPlaces.isRefreshing  = false
                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            placeInserted(doc.document.data)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.i("lugarMod", "modificado")
                            placeModified(doc.document.data)
                        }
                        DocumentChange.Type.REMOVED -> {
                            placeDeleted(doc.document.data)
                        }
                    }
                }
            }
    }

    /**
     * Modifica el documento de la lista y llama al adapter del fragment
     * @param data: Map<String, Any>
     */
    private fun placeModified(data: Map<String, Any>) {
        val place = Mappers.dtoToPlace(data)
        if (places.indexOf(place) >= 0) {
            actualizarPlaceAdapter(place, places.indexOf(place))
        }
    }

    /**
     * Inserta el documento en una lista y llama al adapter del fragment
     * @param data MutableMap<String, Any>
     */
    private fun placeInserted(data: MutableMap<String, Any>) {
        val place = Mappers.dtoToPlace(data)
        val existe = places.any { lugar -> lugar.id == place.id }
        if (!existe)
            insertarPlaceAdapter(place)
        Log.i("lugarinsertado", place.toString())
    }

    /**
     * Elimina el documento de la lista y llama al adapter del fragment
     * @param data: Map<String, Any>
     */
    private fun placeDeleted(data: Map<String, Any>) {
        val place = Mappers.dtoToPlace(data)
        val x = places.indexOf(place)
        Log.i("lugarMod", x.toString())
        if (places.indexOf(place) >= 0) {
            borrarElemento(places.indexOf(place))
        }
    }

    //Borramos elemento del adaptador
    private fun borrarElemento(pos: Int) {
        //Acciones
        val deletedModel: PlaceFB = places[pos]
        adapter.deleteItem(pos)
        adapter.notifyDataSetChanged()
        //Lo borramos
        //deletePlaceBD(deletedModel)
    }

    //Actualizamos el adaptor
    fun actualizarPlaceAdapter(place: PlaceFB, pos: Int) {
        adapter.updateItem(place, pos)
        adapter.notifyDataSetChanged()
    }

    //Insertamos lugar nuevo en el adaptador
    fun insertarPlaceAdapter(place: PlaceFB) {
        adapter.addItem(place)
        adapter.notifyDataSetChanged()
    }

    /**
     * cargamos los lugares y notificamos
     */
    private fun cargarLugares() {
        places.clear()
        placeSwipe_AllPlaces.isRefreshing = true
        adapter = MyPlacesViewModel(places) {
            eventoClicFila(it)
        }
        placeRecycler_AllPlaces.adapter = adapter
        // Avismos que ha cambiado
        //adapter.notifyDataSetChanged()
        //placeRecycler_MyPlaces.setHasFixedSize(true)
        //placeSwipe_MyPlaces.isRefreshing = false
        getDatosFromBD()
        placeSwipe_AllPlaces.isRefreshing = false
    }

    /**
     * Evento click asociado a una fila
     * @param place Places
     */
    private fun eventoClicFila(place: PlaceFB) {
        //initDetailsPlaceFragment(false, place, null, false)
    }
}