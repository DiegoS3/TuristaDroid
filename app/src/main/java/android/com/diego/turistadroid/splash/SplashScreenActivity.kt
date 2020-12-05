package android.com.diego.turistadroid.splash

import android.com.diego.turistadroid.MainActivity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerSession
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.signup.SignUp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashScreenActivity : AppCompatActivity() {

    private val TIME : Long = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        //iniciamos las animaciones
        initAnimations()
        //comprobarSesion()
        initLogin()
    }

    private fun comprobarSesion(){
        val listaSessions = ControllerSession.selectSessions()
        if (listaSessions!!.size > 0){
            initNavigation()
        }else{
            initLogin()
        }
    }

    private fun initLogin() {
        //cargamos la login activity tras pasar X tiempo
        val main = Intent(this, LogInActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(main)
                finish()
            }
        }, this.TIME)
    }

    private fun initNavigation(){
        //cargamos la login activity tras pasar X tiempo
        val main = Intent(this, NavigationDrawer::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(main)
                finish()
            }
        }, this.TIME)
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