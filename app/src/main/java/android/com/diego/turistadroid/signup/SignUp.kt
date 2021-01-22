package android.com.diego.turistadroid.signup

import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerBbdd
import android.com.diego.turistadroid.bbdd.ControllerUser
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.HttpClient
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.UtilsApiImgur
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import io.realm.RealmList
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_input_instagram.*
import kotlinx.android.synthetic.main.layout_input_instagram.view.*
import kotlinx.android.synthetic.main.layout_input_twitter.*
import kotlinx.android.synthetic.main.layout_input_twitter.view.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import okhttp3.OkHttpClient
import java.util.*

class SignUp : AppCompatActivity() {

    private val GALERIA = 0
    private val CAMARA = 1
    private var foto: Uri? = null
    private var nombre = ""
    private var usuario = ""
    private var email = ""
    private var password = ""
    private var ima : Bitmap? = null
    private var instagram = ""
    private var twitter = ""
    private lateinit var clientImgur: OkHttpClient

    companion object{
        var valido = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_sign_up)
        init()
    }

    private fun init(){
        abrirOpciones()
        Utilities.validarPassword(txtPass, progressBar, password_strength, this)
        Utilities.validarEmail(txtEmail, this)
        initSaveDatos()
        redes()
        checkUsuario()
        initClienteImgur()

    }

    private fun initClienteImgur() {
        clientImgur = HttpClient.getClient()!!

    }

    private fun setInsta(insta: String){
        this.instagram = insta
        Log.i("insta2:",instagram)
    }

    private fun setTwitter(twit: String){
        this.twitter = twit
        Log.i("twitter2:",twitter)
    }

    //Insertar id usuario en las redes sociales
    private fun redes(){
        imaInstagramSignUp.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_input_instagram, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView).create()
            mBuilder.show()

            mDialogView.btnOkInsta.setOnClickListener {
                instagram = mDialogView.txtUserNameInstagram.text.toString()
                setInsta(instagram)
                Log.i("insta:",instagram)
                mBuilder.dismiss()

            }

        }
        imaTwitterSignUp.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_input_twitter, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView).create()
            mBuilder.show()

            mDialogView.btnOkTwitter.setOnClickListener {
                twitter = mDialogView.txtUserNameTwitter.text.toString()
                setTwitter(twitter)
                Log.i("twitter:",twitter)
                mBuilder.dismiss()

            }
            Log.i("twitter2:",twitter)
        }
    }

    //Comprobar suario
    private fun checkUsuario(){
        btnRegister.setOnClickListener {
            Log.i("valor de vacios",comprobarVacios().toString())
            if(comprobarVacios()){
                try {

                    if(ControllerUser.uniqueUser(txtNameUser.text.toString())){
                        txtNameUser.error = getString(R.string.errorNameUser)
                    }else{
                        //registrarUsuario()
                        registrarUserApi()
                    }

                }catch (ex: RealmPrimaryKeyConstraintException){
                    txtEmail.error = getString(R.string.errorEmail)
                }
            }else{
                Toast.makeText(applicationContext, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Registrar usuario
    /*
    private fun registrarUsuario() {
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        val imaString = Utilities.bitmapToBase64(imaUser.drawable.toBitmap())
        val listaPlaces = RealmList<Place>()


        val user = imaString?.let { User(txtEmail.text.toString(), txtName.text.toString(),
            txtNameUser.text.toString(), passCifrada, it, listaPlaces, twitter, instagram ) }
        user?.let { ControllerUser.insertUser(it) }
        Toast.makeText(applicationContext, "Usuario Registrado", Toast.LENGTH_SHORT).show()
        val intent = Intent (this, LogInActivity::class.java)
        startActivity(intent)
    }*/

    private fun registrarUserApi(){
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        val imaString = Utilities.bitmapToBase64(imaUser.drawable.toBitmap())
        val data = UtilsApiImgur.uploadImg(this,imaString!!,clientImgur)

        val user = UserApi(
            UUID.randomUUID().toString(),
            txtName.text.toString(),
            txtNameUser.text.toString(),
            txtEmail.text.toString(),
            passCifrada,
            instagram,
            twitter,
            data.getString("link")
        )
        Log.i("userCreado", user.foto.toString())

    }

    //Comprobar campos vaios
    private fun comprobarVacios(): Boolean{
        return Utilities.validarEmail(txtEmail, this) and txtName.text.isNotEmpty() and txtPass.text.isNotEmpty() and txtNameUser.text.isNotEmpty()
    }

    //Dialog para camara o galeria
    private fun abrirOpciones() {
        imaUser.setOnClickListener(){
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView).create()
            mBuilder.show()

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

    //Guardamos datos
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