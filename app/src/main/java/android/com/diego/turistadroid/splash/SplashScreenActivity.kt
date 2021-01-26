package android.com.diego.turistadroid.splash

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.SessionsDTO
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.utilities.Constants
import android.com.diego.turistadroid.utilities.UtilSessions
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var bbddRest: BBDDRest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        //iniciamos las animaciones
        initAnimations()
        bbddRest = BBDDApi.service
        comprobarSesion()
    }

    private fun actualizarSesion(idSesion: String, fecha: String){
        val call = bbddRest.updateDateSession(idSesion, fecha)
        call.enqueue(object : Callback<SessionsDTO>{
            override fun onResponse(call: Call<SessionsDTO>, response: Response<SessionsDTO>) {
                if (response.isSuccessful) {
                    Log.i("sesion", "sesion actualizada")
                } else {
                    Log.i("sesion", "error al actualizar")
                }
            }
            override fun onFailure(call: Call<SessionsDTO>, t: Throwable) {
                Toast.makeText(applicationContext,
                    getString(R.string.errorService),
                    Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun comprobarSesion(){

        val sessionLocal = UtilSessions.getLocal(applicationContext)
        //Si existe una sesion guardada en local
        if (sessionLocal != null){
            val fechaSessionLocal = Utilities.stringToDate(UtilSessions.getFecha(applicationContext))!!
            val idSession = sessionLocal.id!!

            //Si la fecha ha superado el tiempo maximo, eliminamos la sesion local y la remota
            if (comprobarFechas(fechaSessionLocal)){
                Log.i("sesion", "Sesion caducada" )
                UtilSessions.eliminarSesionLocal(applicationContext)
                UtilSessions.eliminarSesionRemota(idSession, bbddRest, this)
                initLogin()
            }else{ //Si no actualizamos la fecha a la del actual inicio de la APP tanto en local como en remoto
                val currentDate = Utilities.dateToString(Utilities.getSysDate())!!
                UtilSessions.actualizarFecha(currentDate, applicationContext)
                actualizarSesion(idSession, currentDate)
                initNavigation()
            }
        }else{
            Log.i("sesion", "No hay sesion" )
            initLogin()
        }
    }

    private fun comprobarFechas(fechaSessionLocal : LocalDateTime) : Boolean{

        val actualDate = Utilities.getSysDate()

        return Utilities.difMinutes(actualDate, fechaSessionLocal) > Constants.MAX_TIME_SESSION

    }

    private fun initLogin() {
        //cargamos la login activity tras pasar X tiempo
        val main = Intent(this, LogInActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(main)
                finish()
            }
        }, Constants.TIME_DELAYED)
    }

    private fun initNavigation(){
        //cargamos la login activity tras pasar X tiempo
        val main = Intent(this, NavigationDrawer::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(main)
                finish()
            }
        }, Constants.TIME_DELAYED)
    }


    /**
     * Metodo que contiene otros metodos que inician las animaciones
     */
    private fun initAnimations(){

        animacionLetras()
        animacionMundo()
        animacionesPines()
    }

    /**
     * Animaciones correspondientes a las letras de la Splash Screen
     */
    private fun animacionLetras() {
        //Animaciones que usaremos
        val animacion1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        val animacion2 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo)

        //Diferentes componentes del layout
        val developText = findViewById<TextView>(R.id.textDevelopment)
        val titleText = findViewById<TextView>(R.id.textTitleSplash)

        //Asignamos la animación correspondiente a cada uno de los componentes
        developText.startAnimation(animacion2)
        titleText.startAnimation(animacion1)
    }

    /**
     * Animaciones correspondientes al mundo del logo del Splash Screen
     */
    private fun animacionMundo(){

        //Animaciones que usaremos
        val animacion3 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_lateral)

        //Diferentes componentes del layout
        val logoImg = findViewById<ImageView>(R.id.imgMundo_Splash)

        //Asignamos la animación correspondiente a cada uno de los componentes
        logoImg.startAnimation(animacion3)

    }
    /**
     * Animaciones correspondientes a los pines del mundo de la Splash Screen
     */
    private fun animacionesPines(){

        //Animaciones que usaremos
        val animacion4 = AnimationUtils.loadAnimation(this, R.anim.fade_pines)
        val animacion5 = AnimationUtils.loadAnimation(this, R.anim.fade_pines1)
        val animacion6 = AnimationUtils.loadAnimation(this, R.anim.fade_pines2)

        //Diferentes componentes del layout
        val pinesImg = findViewById<ImageView>(R.id.imgPines_Splash)
        val pinesImg1 = findViewById<ImageView>(R.id.imgPines1_Splash)
        val pinesImg2 = findViewById<ImageView>(R.id.imgPines2_Splash)

        //Asignamos la animación correspondiente a cada uno de los componentes
        pinesImg.startAnimation(animacion4)
        pinesImg1.startAnimation(animacion5)
        pinesImg2.startAnimation(animacion6)

    }
}