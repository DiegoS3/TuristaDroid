package android.com.diego.turistadroid.signup

import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerBbdd
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.PasswordStrength
import android.com.diego.turistadroid.utilities.Utilities
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*

class SignUp : AppCompatActivity() {

    private val GALERIA = 0
    private val CAMARA = 1
    private var foto: Uri? = null
    private var nombre = ""
    private var usuario = ""
    private var email = ""
    private var password = ""
    private var ima : Bitmap? = null
    private var valido = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        init()
    }

    private fun init(){
        abrirOpciones()
        validarPassword()
        validarEmail()
        initSaveDatos()
        checkUsuario()
    }

    private fun checkUsuario(){
        btnRegister.setOnClickListener {
            Log.i("valor de vacios",comprobarVacios().toString())
            if(comprobarVacios()){
                try {

                    registrarUsuario()

                }catch (ex: RealmPrimaryKeyConstraintException){
                    txtEmail.error = getString(R.string.errorEmail)
                }
            }else{
                Toast.makeText(applicationContext, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarUsuario() {
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        val imaString = Utilities.bitmapToBase64(imaUser.drawable.toBitmap())
        val user = imaString?.let { User(txtEmail.text.toString(), txtName.text.toString(),
            txtNameUser.text.toString(), passCifrada, it) }
        user?.let { ControllerBbdd.insertUser(it) }
        Toast.makeText(applicationContext, "Usuario Registrado", Toast.LENGTH_SHORT).show()
        val intent = Intent (this, LogInActivity::class.java)
        startActivity(intent)
    }

    private fun comprobarVacios(): Boolean{
        return validarEmail() and txtName.text.isNotEmpty() and txtPass.text.isNotEmpty() and txtNameUser.text.isNotEmpty()
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
        txtPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrengthView(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun validarEmail(): Boolean{

        txtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()) {
                    valido = true
                } else {
                    valido = false
                    txtEmail.error = "Invalid Email"
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        Log.i("contraseña bool",valido.toString())
        return valido


    }


    private fun abrirOpciones() {
        imaUser.setOnClickListener(){
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView).create()
            val mAlertDialog = mBuilder.show()

            //Listener para abrir la camara
            mDialogView.txtCamara.setOnClickListener {
                abrirCamara()
                mBuilder.dismiss()
            }

            //Listener para abrir la galeria
            mDialogView.txtGaleria.setOnClickListener {
                abrirGaleria()
                mBuilder.dismiss()
            }
        }
    }

    //muestro la camara
    private fun abrirCamara(){
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permisosCamara =
                    arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permisosCamara, CAMARA)
            }else{
                mostrarCamara()
            }
    }

    //abre la camara y hace la foto
    private fun mostrarCamara(){
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        foto = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, foto)
        startActivityForResult(intent, CAMARA)
    }

    //pido los permisos para abrir la galeria
    private fun abrirGaleria(){
            val permiso = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permiso, GALERIA)
    }


    //obtengo el resultado de pedir los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            GALERIA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mostrarGaleria()
                else
                    Toast.makeText(
                        applicationContext,
                        "No tienes permiso para acceder a la galería",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            CAMARA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    abrirCamara()
                else
                    Toast.makeText(applicationContext, "No tienes permiso para acceder a la cámara", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    //si el usuario selecciona una imagen, la pongo en el imagenView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode==GALERIA) {
            imaUser.setImageURI(data?.data)
            Utilities.redondearFoto(imaUser)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            imaUser.setImageURI(foto)
            Utilities.redondearFoto(imaUser)
        }
    }

    //muetsro la galeria
    private fun mostrarGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALERIA)
    }



    override fun onStop() {
        super.onStop()
        ControllerBbdd.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        ControllerBbdd.close()
    }

    private fun initSaveDatos(){
        nombre = txtName.text.toString()
        usuario = txtNameUser.text.toString()
        email = txtEmail.text.toString()
        password = txtPass.text.toString()
        ima = imaUser.drawable.toBitmap()
    }

    // Para salvar el estado por ejemplo es usando un Bundle en el ciclo de vida
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("EMAIL", email)
            putString("NOMBRE", nombre)
            putString("USUARIO", usuario)
            putString("IMAGEN", ima?.let { Utilities.bitmapToBase64(it) })
            putString("PWD", password)
        }
        // Siempre se llama a la superclase para salvar las cosas
        super.onSaveInstanceState(outState)
    }

    // Para recuperar el estado al volver al un estado de ciclo de vida de la Interfaz
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Recuperamos en un bundle estas variables o estados de la interfaz
        super.onRestoreInstanceState(savedInstanceState)
        // Recuperamos del Bundle
        savedInstanceState.run {
            email= getString("EMAIL").toString()
            nombre = getString("NOMBRE").toString()
            usuario = getString("USUARIO").toString()
            ima = Utilities.base64ToBitmap(getString("IMAGEN").toString())
            password = getString("PWD").toString()
        }
    }

}