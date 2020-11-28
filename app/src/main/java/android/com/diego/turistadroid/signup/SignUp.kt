package android.com.diego.turistadroid.signup

import android.com.diego.turistadroid.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*

class SignUp : AppCompatActivity() {

    private val GALERIA = 0
    private val CAMARA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        init()

    }

    private fun init(){
        abrirOpciones()
        abrirCamara()
        abrirGaleria()
    }


    private fun abrirOpciones() {
        imaUser.setOnClickListener(){
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView).create()
            val mAlertDialog = mBuilder.show()

            //Listener para abrir la camara
            mDialogView.txtCamara.setOnClickListener {

            }

            //Listener para abrir la galeria
            mDialogView.txtGaleria.setOnClickListener {

            }
        }
    }

    private fun abrirCamara() {

    }

    private fun abrirGaleria() {

    }




}