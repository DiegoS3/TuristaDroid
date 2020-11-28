package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.signup.SignUp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_log_in)

        abrirRegistro()
    }

    fun abrirRegistro(){
        textRegister_LogIn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

    }
}