package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.newplace.NewPlaceFragment
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_myplaces.*


class MyPlacesFragment : Fragment() {

    // Mis variables
    private var places = mutableListOf<Place>() // Lista
    private lateinit var place : Place
    private val user = LogInActivity.user //Usuario logeado
    private var clicked = false

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

    private fun initUI(){
        initFloatingButtons()
        cargarDatos()

        // Mostramos las vistas de listas y adaptador asociado
        placeRecycler_MyPlaces.layoutManager = LinearLayoutManager(context)
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

    private fun initFloatingButtons(){

        btnFloatAddPlace_MyPlaces.setOnClickListener{
            onAddButtonClicked()
        }

        btnFloatAddNewPlace.setOnClickListener{
            initNewPlaceFragment()
        }

        btnFloatAddActualPlace.setOnClickListener {
            Toast.makeText(context, "Nuevo Lugar Actual", Toast.LENGTH_SHORT).show()
        }

    }

    private fun onAddButtonClicked() {

        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)

        clicked = !clicked
    }

    private fun initNewPlaceFragment(){

        val newFragment: Fragment = NewPlaceFragment()
        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked){
            btnFloatAddActualPlace.visibility = View.VISIBLE
            btnFloatAddNewPlace.visibility = View.VISIBLE
            txtAddActualPlace.visibility = View.VISIBLE
            txtAddNewPlace.visibility = View.VISIBLE
        }else{
            btnFloatAddActualPlace.visibility = View.INVISIBLE
            btnFloatAddNewPlace.visibility = View.INVISIBLE
            txtAddActualPlace.visibility = View.INVISIBLE
            txtAddNewPlace.visibility = View.INVISIBLE
        }
    }

    private fun setVisibility(clicked: Boolean) {

        //Animaciones
        val rotateOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim)
        val rotateClose = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim)
        val fromBottom = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim)
        val toBottom = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim)

        if (!clicked){
            btnFloatAddActualPlace.startAnimation(fromBottom)
            btnFloatAddNewPlace.startAnimation(fromBottom)
            txtAddActualPlace.startAnimation(fromBottom)
            txtAddNewPlace.startAnimation(fromBottom)
            btnFloatAddPlace_MyPlaces.startAnimation(rotateOpen)

        }else{
            btnFloatAddActualPlace.startAnimation(toBottom)
            btnFloatAddNewPlace.startAnimation(toBottom)
            txtAddActualPlace.startAnimation(toBottom)
            txtAddNewPlace.startAnimation(toBottom)
            btnFloatAddPlace_MyPlaces.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean){
        if (!clicked){
            btnFloatAddActualPlace.isClickable = true
            btnFloatAddNewPlace.isClickable = true
        }else{
            btnFloatAddActualPlace.isClickable = false
            btnFloatAddNewPlace.isClickable = false
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
        //places = ControllerPlaces.selectPlaces()!!
        places = user.places

    }

    /**
     * Evento cli asociado a una fila
     * @param place Place
     */
    private fun eventoClicFila(place: Place) {
        // Creamos el dialogo y casamos sus elementos
        //Toast.makeText(context, "PULSADO datos", Toast.LENGTH_LONG).show()
        //abrirPruebas()
        //val pruebaFragment = PruebaFragment(NavigationActivity.user, dato)
        //abrirPrueba(pruebaFragment)
    }

    inner class TareaCargarDatos : AsyncTask<String?, Void?, Void?>() {
        /**
         * Acciones antes de ejecutarse
         */
        override fun onPreExecute() {
            if (placeSwipe_MyPlaces.isRefreshing) {
                placeSwipe_MyPlaces.isRefreshing = false
            }
            Toast.makeText(context, "Obteniendo datos", Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg p0: String?): Void? {
            Log.d("Datos", "Entrado en doInBackgroud");
            try {
                getDatosFromBD()
                Log.d("Datos", "Datos pre tamaño: " + places.size.toString());
            } catch (e: Exception) {
                Log.e("T2Plano ", e.message.toString());
            }
            Log.d("Datos", "onDoInBackgroud OK");
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
            Log.d("Datos", "onPostExecute OK");
            Log.d("Datos", "Datos post tam: " + places.size.toString());
            Toast.makeText(context, "Datos cargados", Toast.LENGTH_LONG).show()
        }
    }
}