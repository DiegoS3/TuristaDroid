package android.com.diego.turistadroid.signup

import android.com.diego.turistadroid.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    private val req_galeria = 0
    private val req_camara = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿De dónde quieres seleccionar la foto?")



        abrirOpciones()
        abrirCamara()
        abrirGaleria()
    }

    private fun abrirOpciones() {
        btnRegister.setOnClickListener(){

        }
    }


    private fun abrirCamara() {

    }

    private fun abrirGaleria() {

    }




}