package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

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
import android.com.diego.turistadroid.bbdd.firebase.entities.ImageFB
import android.com.diego.turistadroid.bbdd.firebase.entities.PlaceFB
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.com.diego.turistadroid.bbdd.firebase.mappers.Mappers
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewActualPlaceFragment
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewPlaceFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.android.synthetic.main.fragment_myplaces.*
import kotlinx.android.synthetic.main.layout_confirm_delete_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MyPlacesFragment : Fragment() {

    // Mis variables
    private var places = mutableListOf<PlaceFB>() // Lista
    private lateinit var place: PlaceFB
    private var clicked = false
    private var clickedSort = false
    private lateinit var placeQr: PlaceFB
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

        root = inflater.inflate(R.layout.fragment_myplaces, container, false)
        myContext = context!!
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
        placeRecycler_MyPlaces.layoutManager = LinearLayoutManager(context)
        cargarLugares()
        iniciarSwipeHorizontal()
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
        placeSwipe_MyPlaces.setColorSchemeResources(R.color.colorPrimaryDark)
        placeSwipe_MyPlaces.setProgressBackgroundColorSchemeResource(R.color.colorDialog)
        placeSwipe_MyPlaces.setOnRefreshListener {
            cargarLugares()
            //getDatosFromBD()
        }
    }

    /**
     * Realiza el swipe horizontal para eliminar o editar un sitio
     */
    private fun iniciarSwipeHorizontal() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {
            // Sobreescribimos los métodos
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Analizamos el evento según la dirección
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Si pulsamos a la de izquierda o a la derecha
                // Programamos la accion
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        abrirOpciones(position)
                    }
                    else -> {
                        editarElemento(position)
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3
                    // Si es dirección a la derecha: izquierda->derecta
                    // Pintamos de azul y ponemos el icono
                    if (dX > 0) {
                        // Pintamos el botón izquierdo
                        botonIzquierdo(canvas, dX, itemView, width)

                    } else {
                        // Caso contrario
                        botonDerecho(canvas, dX, itemView, width)
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        // Añadimos los eventos al RV
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(placeRecycler_MyPlaces)
    }


    //Eliminamos la imagen del lugar
    private fun deleteImgPlace(id: String) {
        val foto_ref = storage_ref.child("/imagenes/${id}")
        foto_ref.listAll().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (img in task.result.items)
                    img.delete()
                Log.i("deleteimage", "ok")
            } else
                Log.i("deleteimage", "no ok")
        }
    }

    //Eliminamos el lugar
    private fun deletePlaceBD(place: PlaceFB) {
        eliminarImagesPlace(place)
        eliminarVotosPlace(place)

        FireStore.collection("places")
            .document(place.id!!)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    deleteImgPlace(place.id)
            }
    }

    private fun eliminarImagesPlace(place: PlaceFB) {
        FireStore.collection("places")
            .document(place.id!!)
            .collection("images")
            .get()
            .addOnSuccessListener { task ->
                for (ima in task.documents) {
                    val image = Mappers.dtoToImage(ima.data!!)
                    FireStore.collection("places")
                        .document(place.id)
                        .collection("images")
                        .document(image.id!!).delete()
                }
            }
    }

    private fun eliminarVotosPlace(place: PlaceFB) {
        FireStore.collection("places")
            .document(place.id!!)
            .collection("votos")
            .get()
            .addOnSuccessListener { task ->
                for (votos in task.documents) {
                    val voto = Mappers.dtoToVoto(votos.data!!)
                    FireStore.collection("places")
                        .document(place.id)
                        .collection("votos")
                        .document(voto.idUser!!).delete()
                }
            }
    }

    //Borramos elemento del adaptador
    private fun borrarElemento(pos: Int) {
        //Acciones
        val deletedModel: PlaceFB = places[pos]
        adapter.deleteItem(pos)
        adapter.notifyDataSetChanged()
        //Lo borramos
        deletePlaceBD(deletedModel)
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

    //inciamos fragment details en modo edicion
    private fun editarElemento(pos: Int) {
        //initDetailsPlaceFragment(true, places[pos], pos, false)
    }

    //Dialog para que confirme el si quiere eliminar el lugar
    private fun abrirOpciones(pos: Int) {
        //cargarDatos()
        cargarLugares()
        val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.layout_confirm_delete_item, null)
        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView).create()
        mBuilder.show()

        //Listener para confirmar eliminar el lugar
        mDialogView.txtConfirm.setOnClickListener {
            borrarElemento(pos)
            Utilities.vibratePhone(context)
            mBuilder.dismiss()
        }

        //Listener para confirmar cancelar el lugar
        mDialogView.txtCancel.setOnClickListener {
            mBuilder.dismiss()
        }
    }

    /**
     * Mostramos el elemento derecho
     * @param canvas Canvas
     * @param dX Float
     * @param itemView View
     * @param width Float
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de rojo y ponemos el icono
        paintSweep.color = resources.getColor(R.color.colorDeletePlace)
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sweep_eliminar)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Mostramos el elemento izquierdo
     * @param canvas Canvas
     * @param dX Float
     * @param itemView View
     * @param width Float
     */
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de azul y ponemos el icono
        paintSweep.color = resources.getColor(R.color.colorToolBar)
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sweep_detalles)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    private fun initFloatingButtons() {

        btnFloatAddPlace_MyPlaces.setOnClickListener {
            onAddButtonClicked()
        }

        btnFloatAddNewPlace.setOnClickListener {
            initNewActualPlaceFragment()
        }

        btnFloatAddActualPlace.setOnClickListener {
            initNewPlaceFragment()
        }
        btnFloatAddQRPlace.setOnClickListener {
            scanQRCode()
        }
        btnSortPlaces_MyPlaces.setOnClickListener {
            onSortButtonClicked()
        }
    }

    //Metodo al hacer click en el FAB añadir
    private fun onAddButtonClicked() {

        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    //Metodo al hacer click en el FAB ordenar
    private fun onSortButtonClicked() {

        setVisibilitySort(clickedSort)
        setAnimationSort(clickedSort)
        setClickableSort(clickedSort)
        clickedSort = !clickedSort
    }

    //Iniciamos fragment Nuevo Lugar
    private fun initNewPlaceFragment() {
        val newFragment: Fragment = NewPlaceFragment(userFB)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Iniciamos fragment Nuevo Lugar Actual
    private fun initNewActualPlaceFragment() {
        val newFragment: Fragment = NewActualPlaceFragment(userFB)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
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
            val newFragment: Fragment = MyPlaceDetailFragment(editable, place, pos, this, import, userApi)
            val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()

         */
    }

    //Modificar visibilidad FABS ADD
    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            btnFloatAddActualPlace.visibility = View.VISIBLE
            btnFloatAddNewPlace.visibility = View.VISIBLE
            btnFloatAddQRPlace.visibility = View.VISIBLE
            txtAddActualPlace.visibility = View.VISIBLE
            txtAddNewPlace.visibility = View.VISIBLE
            txtAddQRPlace.visibility = View.VISIBLE
        } else {
            btnFloatAddActualPlace.visibility = View.INVISIBLE
            btnFloatAddNewPlace.visibility = View.INVISIBLE
            btnFloatAddQRPlace.visibility = View.INVISIBLE
            txtAddActualPlace.visibility = View.INVISIBLE
            txtAddNewPlace.visibility = View.INVISIBLE
            txtAddQRPlace.visibility = View.INVISIBLE
        }
    }

    //Añadir animaciones on clikc FAB ADD
    private fun setAnimation(clicked: Boolean) {

        //Animaciones
        val rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim)
        val rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim)
        val fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        val toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)

        if (!clicked) {
            btnFloatAddActualPlace.startAnimation(fromBottom)
            btnFloatAddNewPlace.startAnimation(fromBottom)
            btnFloatAddQRPlace.startAnimation(fromBottom)
            txtAddActualPlace.startAnimation(fromBottom)
            txtAddNewPlace.startAnimation(fromBottom)
            txtAddQRPlace.startAnimation(fromBottom)
            btnFloatAddPlace_MyPlaces.startAnimation(rotateOpen)

        } else {
            btnFloatAddActualPlace.startAnimation(toBottom)
            btnFloatAddNewPlace.startAnimation(toBottom)
            btnFloatAddQRPlace.startAnimation(toBottom)
            txtAddActualPlace.startAnimation(toBottom)
            txtAddNewPlace.startAnimation(toBottom)
            txtAddQRPlace.startAnimation(toBottom)
            btnFloatAddPlace_MyPlaces.startAnimation(rotateClose)
        }
    }

    //Modificar clickable FAB ADD
    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            btnFloatAddActualPlace.isClickable = true
            btnFloatAddNewPlace.isClickable = true
            btnFloatAddQRPlace.isClickable = true
        } else {
            btnFloatAddActualPlace.isClickable = false
            btnFloatAddNewPlace.isClickable = false
            btnFloatAddQRPlace.isClickable = false
        }
    }

    //Visibilidad FABS SORT
    private fun setVisibilitySort(clickedSort: Boolean) {
        if (!clickedSort) {
            btnSortNamePlace.visibility = View.VISIBLE
            btnSortDatePlace.visibility = View.VISIBLE
            btnSortMarkPlace.visibility = View.VISIBLE
        } else {
            btnSortNamePlace.visibility = View.INVISIBLE
            btnSortDatePlace.visibility = View.INVISIBLE
            btnSortMarkPlace.visibility = View.INVISIBLE
        }
    }

    //Animacion FABS Sort
    private fun setAnimationSort(clickedSort: Boolean) {

        //Animaciones
        val fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        val toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)

        if (!clickedSort) {
            btnSortNamePlace.startAnimation(fromBottom)
            btnSortDatePlace.startAnimation(fromBottom)
            btnSortMarkPlace.startAnimation(fromBottom)

        } else {
            btnSortNamePlace.startAnimation(toBottom)
            btnSortDatePlace.startAnimation(toBottom)
            btnSortMarkPlace.startAnimation(toBottom)
        }
    }

    //Clickables FABS SORt
    private fun setClickableSort(clickedSort: Boolean) {
        if (!clickedSort) {
            btnSortNamePlace.isClickable = true
            btnSortDatePlace.isClickable = true
            btnSortMarkPlace.isClickable = true
        } else {
            btnSortNamePlace.isClickable = false
            btnSortDatePlace.isClickable = false
            btnSortMarkPlace.isClickable = false
        }
    }

    /**
     * Escanea el código
     */
    private fun scanQRCode() {
        val integrator = IntentIntegrator.forSupportFragment(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt(getString(R.string.scanQR))
        }
        integrator.initiateScan()
    }

    /**
     * Procesamos los resultados
     * @param requestCode Int
     * @param resultCode Int
     * @param data [ERROR : Intent]
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(context, getString(R.string.btnCancel), Toast.LENGTH_LONG).show()
            } else {
                try {
                    placeQr = Gson().fromJson(result.contents, PlaceFB::class.java)
                    //initDetailsPlaceFragment(false, placeQr, null, true)
                } catch (ex: Exception) {
                    Toast.makeText(context, getString(R.string.errorEmail), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //Metodo que ordena sitios y modifica iconos de los FABS
    private fun orderSites() {

        btnSortNamePlace.setOnClickListener { // Order by NAME
            ascName = if (ascName) {
                btnSortNamePlace.setImageResource(R.drawable.ic_short_name_desc_btn)
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_name_asc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar1.name!!.toLowerCase().compareTo(lugar2.name!!.toLowerCase())
                }
                false
            } else {
                btnSortNamePlace.setImageResource(R.drawable.ic_short_name_asc_btn)
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_name_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar2.name!!.toLowerCase().compareTo(lugar1.name!!.toLowerCase())
                }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortDatePlace.setOnClickListener { // Order by DATE
            ascDate = if (ascDate) {
                btnSortDatePlace.setImageResource(R.drawable.ic_short_date_desc_btn)
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_date_asc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha1!!.compareTo(fecha2!!)
                }
                false
            } else {
                btnSortDatePlace.setImageResource(R.drawable.ic_short_date_asc_btn)
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_date_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    val fecha1 = Utilities.stringToDate(lugar1.fecha!!)
                    val fecha2 = Utilities.stringToDate(lugar2.fecha!!)
                    fecha2!!.compareTo(fecha1!!)
                }
                true
            }
            adapter.notifyDataSetChanged()
        }

        btnSortMarkPlace.setOnClickListener { // Order by RATINGS
            ascMark = if (ascMark) {
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlace.setImageResource(R.drawable.ic_short_mark_desc_btn)
                this.places.sortWith { lugar1: PlaceFB, lugar2: PlaceFB ->
                    lugar2.votos!!.toInt().compareTo(lugar1.votos!!.toInt())
                }
                false
            } else {
                btnSortPlaces_MyPlaces.setImageResource(R.drawable.ic_short_mark_asc_btn)
                btnSortMarkPlace.setImageResource(R.drawable.ic_short_mark_desc_btn)
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
        FireStore.collection("places").whereEqualTo("idUser", idUser)
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

    /**
     * cargamos los lugares y notificamos
     */
    private fun cargarLugares() {
        places.clear()
        placeSwipe_MyPlaces.isRefreshing = true
        adapter = MyPlacesViewModel(places) {
            eventoClicFila(it)
        }
        placeRecycler_MyPlaces.adapter = adapter
        // Avismos que ha cambiado
        //adapter.notifyDataSetChanged()
        //placeRecycler_MyPlaces.setHasFixedSize(true)
        //placeSwipe_MyPlaces.isRefreshing = false
        getDatosFromBD()
        placeSwipe_MyPlaces.isRefreshing = false
    }

    /**
     * Evento click asociado a una fila
     * @param place Places
     */
    private fun eventoClicFila(place: PlaceFB) {
        //initDetailsPlaceFragment(false, place, null, false)
    }

}