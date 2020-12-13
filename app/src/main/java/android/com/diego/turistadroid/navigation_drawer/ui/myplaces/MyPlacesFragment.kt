package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.*
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewActualPlaceFragment
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewPlaceFragment
import android.com.diego.turistadroid.utilities.Utilities
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
import com.google.gson.Gson
import com.google.zxing.client.result.VINParsedResult
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.android.synthetic.main.fragment_myplaces.*
import kotlinx.android.synthetic.main.layout_confirm_delete_item.view.*
import java.util.concurrent.ConcurrentLinkedQueue


class MyPlacesFragment : Fragment() {

    // Mis variables
    private var places = mutableListOf<Place>() // Lista
    private lateinit var place: Place
    private val user = LogInActivity.user //Usuario logeado
    private var clicked = false
    private lateinit var placeQr : Place

    // Interfaz gráfica
    private lateinit var adapter: MyPlacesViewModel //Adaptador de Recycler
    private lateinit var tarea: TareaCargarDatos // Tarea en segundo plano
    private var paintSweep = Paint()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myplaces, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Iniciamos la interfaz
        initUI()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun initUI() {
        initFloatingButtons()
        iniciarSwipeRecarga()
        iniciarSwipeHorizontal()
        // Mostramos las vistas de listas y adaptador asociado
        placeRecycler_MyPlaces.layoutManager = LinearLayoutManager(context)
        cargarDatos()
    }

    /**
     * Iniciamos el swipe de recarga
     */
    private fun iniciarSwipeRecarga() {
        placeSwipe_MyPlaces.setColorSchemeResources(R.color.colorPrimaryDark)
        //datosSwipe.setProgressBackgroundColorSchemeResource(R.color.design_default_color_primary)
        placeSwipe_MyPlaces.setOnRefreshListener {
            cargarDatos()
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

    private fun deleteImgPlace(place: Place) {

        val list = ConcurrentLinkedQueue<Image>()

        for (img in place.imagenes) {
            list.add(img)
        }
        list.forEachIndexed { index, image ->
            place.imagenes.remove(image)
            ControllerImages.deleteImage(image.id)
        }
    }

    private fun restoreImgPlace(place: Place) {
        val list = ConcurrentLinkedQueue<Image>()

        for (img in place.imagenes) {
            list.add(img)
        }

        list.forEachIndexed { index, image ->

            val id = ControllerImages.getImageIdentity()
            val imag = Image(id, image.foto)
            place.imagenes.add(imag)
            ControllerImages.insertImage(imag)

        }
    }

    private fun restorePlaceBD(place: Place) {
        restoreImgPlace(place)
        user.places.add(place)
        val newUser = User(user.email, user.nombre, user.nombreUser, user.pwd, user.foto, user.places)
        ControllerUser.updateUser(newUser)
        val idPlace = ControllerPlaces.getPlaceIdentity()
        val newPlace = Place(
            idPlace,
            place.nombre,
            place.fecha,
            place.city,
            place.puntuacion,
            place.longitud,
            place.latitud,
            place.imagenes
        )
        ControllerPlaces.updatePlace(newPlace)
    }

    private fun deletePlaceBD(place: Place) {

        deleteImgPlace(place)
        user.places.remove(place)
        val newUser = User(user.email, user.nombre, user.nombreUser, user.pwd, user.foto, user.places)
        ControllerUser.updateUser(newUser)
        ControllerPlaces.deletePlace(place.id)
    }

    private fun borrarElemento(pos: Int) {
        //Acciones
        val deletedModel: Place = places[pos]
        adapter.deleteItem(pos)
        //Lo borramos
        deletePlaceBD(deletedModel)
        adapter.notifyDataSetChanged()
    }

    fun actualizarPlaceAdapter(place: Place, pos: Int){
        adapter.updateItem(place, pos)
        adapter.notifyDataSetChanged()
    }

    fun insertarPlaceAdapter(place: Place){
        adapter.addItem(place)
        adapter.notifyDataSetChanged()
    }

    private fun editarElemento(pos: Int){
        initDetailsPlaceFragment(true, places[pos], pos, false)
    }

    private fun abrirOpciones(pos: Int) {
        cargarDatos()
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
    }

    private fun onAddButtonClicked() {

        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun initNewPlaceFragment() {

        val newFragment: Fragment = NewPlaceFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initNewActualPlaceFragment() {

        val newFragment: Fragment = NewActualPlaceFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initDetailsPlaceFragment(boolean: Boolean, place: Place, pos: Int?, import : Boolean) {

        val newFragment: Fragment = MyPlaceDetailFragment(boolean, place, pos, this, import)
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

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
                Toast.makeText(context, "Cancelado", Toast.LENGTH_LONG).show()
            }
            else {
                try {
                    placeQr = Gson().fromJson(result.contents, Place::class.java)
                    // Toast.makeText(context, "Recuperado: $LUGAR", Toast.LENGTH_LONG).show()
                    //initUI()
                    initDetailsPlaceFragment(false, placeQr, null, true)
                } catch (ex: Exception) {
                    Toast.makeText(context, "Error: El QR no es de un lugar válido", Toast.LENGTH_LONG).show()
                    // volver()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Carga las datos
     */
    private fun cargarDatos() {
        tarea = TareaCargarDatos()
        tarea.execute()
    }

    fun getDatosFromBD() {
        // Seleccionamos los lugares
        places = user.places
        //places = ControllerPlaces.selectPlaces()!!
    }

    /**
     * Evento cli asociado a una fila
     * @param place Place
     */
    private fun eventoClicFila(place: Place) {
        initDetailsPlaceFragment(false, place, null, false)
    }

    inner class TareaCargarDatos : AsyncTask<String?, Void?, Void?>() {
        /**
         * Acciones antes de ejecutarse
         */
        override fun onPreExecute() {
            if (placeSwipe_MyPlaces.isRefreshing) {
                placeSwipe_MyPlaces.isRefreshing = false
            }
        }

        override fun doInBackground(vararg p0: String?): Void? {
            Log.d("Datos", "Entrado en doInBackgroud")
            try {
                getDatosFromBD()
                Log.d("Datos", "Datos pre tamaño: " + places.size.toString())
            } catch (e: Exception) {
                Log.e("T2Plano ", e.message.toString())
            }
            Log.d("Datos", "onDoInBackgroud OK")
            return null
        }

        /**
         * Procedimiento a realizar al terminar
         * Cargamos la lista
         *
         * @param args
         */
        override fun onPostExecute(args: Void?) {
            Log.d("Datos", "entrando en onPostExecute")
            adapter = MyPlacesViewModel(places) {
                eventoClicFila(it)
                place = it
            }

            placeRecycler_MyPlaces.adapter = adapter
            // Avismos que ha cambiado
            adapter.notifyDataSetChanged()
            placeRecycler_MyPlaces.setHasFixedSize(true)
            placeSwipe_MyPlaces.isRefreshing = false
            Log.d("Datos", "onPostExecute OK")
            Log.d("Datos", "Datos post tam: " + places.size.toString())
        }
    }
}