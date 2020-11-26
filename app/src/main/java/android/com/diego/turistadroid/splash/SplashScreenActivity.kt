package android.com.diego.turistadroid.splash

import android.com.diego.turistadroid.MainActivity
import android.com.diego.turistadroid.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashScreenActivity : AppCompatActivity() {

    private val TIME : Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        val animacion1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        val animacion2 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo)
        val animacion3 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_lateral)
        val animacion4 = AnimationUtils.loadAnimation(this, R.anim.fade_pines)

        val developText = findViewById<TextView>(R.id.textDevelopment)
        val titleText = findViewById<TextView>(R.id.textTitleSplash)
        val logoImg = findViewById<ImageView>(R.id.imgMundo_Splash)
        val pinesImg = findViewById<ImageView>(R.id.imgPines_Splash)

        developText.startAnimation(animacion2)
        titleText.startAnimation(animacion1)
        logoImg.startAnimation(animacion3)
        pinesImg.startAnimation(animacion4)

        Handler(Looper.getMainLooper()).postDelayed({
            run {



            }
        }, 1000)

        val main = Intent(this, MainActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                startActivity(main)
                finish()
            }
        }, this.TIME)


    }
}