package android.com.diego.turistadroid.signup

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.utilities.PasswordStrength
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
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
        validarPassword()
        validarEmail()
    }

    private fun updatePasswordStrengthView(password: String) {

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val strengthView = findViewById<ProgressBar>(R.id.password_strength) as TextView
        if (TextView.VISIBLE != strengthView.visibility)
            return

        if (TextUtils.isEmpty(password)) {
            strengthView.text = ""
            progressBar.progress = 0
            return
        }

        val str = PasswordStrength.calculateStrength(password)
        strengthView.text = str.getText(this)
        strengthView.setTextColor(str.color)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            progressBar.progressDrawable.colorFilter = BlendModeColorFilter(str.color, BlendMode.SRC_IN)
        } else {
            progressBar.progressDrawable.setColorFilter(str.color, PorterDuff.Mode.SRC_IN)
        }
        when {
            str.getText(this) == "Weak" -> {
                progressBar.progress = 25
            }
            str.getText(this) == "Medium" -> {
                progressBar.progress = 50
            }
            str.getText(this) == "Strong" -> {
                progressBar.progress = 75
            }
            else -> {
                progressBar.progress = 100
            }
        }
    }

    private fun validarPassword(){
        txtPass.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrengthView(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun validarEmail(){

        txtEmail.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()){
                    btnRegister.isEnabled = true
                }else{
                    btnRegister.isEnabled = false
                    txtEmail.error = "Invalid Email"
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

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