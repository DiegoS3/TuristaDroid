package android.com.diego.turistadroid.signup

import android.app.Activity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.HttpClient
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.layout_input_instagram.view.*
import kotlinx.android.synthetic.main.layout_input_twitter.view.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.*

class SignUp : AppCompatActivity() {

    private val GALERIA = 0
    private val CAMARA = 1
    private var foto: Uri? = null
    private var nombre = ""
    private var usuario = ""
    private var email = ""
    private var password = ""
    private var ima: Bitmap? = null
    private var instagram = ""
    private var twitter = ""
    private lateinit var clientImgur: OkHttpClient
    private lateinit var bbddRest: BBDDRest
    private lateinit var loadingView: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_sign_up)
        init()
    }

    private fun init() {

        abrirOpciones()
        Utilities.validarPassword(txtPass, progressBar, password_strength, this)
        Utilities.validarEmail(txtEmail, this)
        initSaveDatos()
        redes()
        checkUsuario()
        initClients()
        initDialog()
    }

    private fun initDialog(){

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        loadingView = builder.create()
    }

    //clientes para las conexiones con las API de las que consumimos datos
    private fun initClients() {

        clientImgur = HttpClient.getClient()!!
        bbddRest = BBDDApi.service

    }

    private fun setInsta(insta: String) {
        this.instagram = insta
        Log.i("insta2:", instagram)
    }

    private fun setTwitter(twit: String) {
        this.twitter = twit
        Log.i("twitter2:", twitter)
    }

    //Insertar id usuario en las redes sociales
    private fun redes() {
        imaInstagramSignUp.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_input_instagram, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView).create()
            mBuilder.show()

            mDialogView.btnOkInsta.setOnClickListener {
                instagram = mDialogView.txtUserNameInstagram.text.toString()
                setInsta(instagram)
                Log.i("insta:", instagram)
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
                Log.i("twitter:", twitter)
                mBuilder.dismiss()

            }
            Log.i("twitter2:", twitter)
        }
    }

    /**
     * Comprobamos que el email no exista en la bbddd
     * tras haber hecho la comprobación de que el userName
     * sea unico
     */
    private fun uniqueEmail(){
        val email = txtEmail.text.toString()
        val call = bbddRest.selectUserByEmail(email)

        call.enqueue((object : retrofit2.Callback<List<UserDTO>> {
            override fun onResponse(call: retrofit2.Call<List<UserDTO>>, response: retrofit2.Response<List<UserDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {

                    //Si el body no esta vacio ese email ya esta registrado
                    if(response.body()!!.isNotEmpty()){
                        txtEmail.error = getString(R.string.errorEmail)
                    }else{ //en caso contrario no existe y permitimos el registro en la bbdd
                        uploadImgToImgurAPI()
                    }

                } else {

                    Toast.makeText(applicationContext, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            //Si error
            override fun onFailure(call: retrofit2.Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }
        }))
    }

    /**
     * Comprobamos que el userName que introduce
     * el usuario no exista en nuestra bbdd
     */
    private fun uniqueUser(nameUser: String) {

        val call = bbddRest.selectUserByUserName(nameUser)

        call.enqueue((object : retrofit2.Callback<List<UserDTO>> {
            override fun onResponse(call: retrofit2.Call<List<UserDTO>>, response: retrofit2.Response<List<UserDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {

                    //Si el cuerpo no esta vacio el usuario existe mostramos error
                    if(response.body()!!.isNotEmpty()){
                        txtNameUser.error = getString(R.string.errorNameUser)
                    }else{
                        uniqueEmail() //Si no es que no existe procedemos a comprobar el email
                    }

                } else {

                    Toast.makeText(applicationContext, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            //Si error
            override fun onFailure(call: retrofit2.Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }
        }))
    }

    //Comprobar suario
    private fun checkUsuario() {
        btnRegister.setOnClickListener {

            if (comprobarVacios()) {
                uniqueUser(txtNameUser.text.toString())
            } else {
                Toast.makeText(applicationContext, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Subimos la imagen que ha elegido el usuario a la
     * Api de IMGUR y creamos el usuario que posteriormente
     * registramos en la bbdd de nuestra API
     */
    private fun uploadImgToImgurAPI() {
        loadingView.show()
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        val imaString = Utilities.bitmapToBase64(imaUser.drawable.toBitmap())!!

        val mediaType: MediaType = "text/plain".toMediaTypeOrNull()!!
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", imaString)
            .build()
        val request = ImgurREST.postImage(body, "base64")
        Log.i("answer", request.toString() + " " + request.body.toString())
        clientImgur.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(applicationContext, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {

                    val data = JSONObject(response.body!!.string())
                    val item = data.getJSONObject("data")
                    val user = UserApi(
                        UUID.randomUUID().toString(),
                        txtName.text.toString(),
                        txtNameUser.text.toString(),
                        txtEmail.text.toString(),
                        passCifrada,
                        instagram,
                        twitter,
                        item.getString("link")
                    )
                    insertUserApi(user)

                } else {
                    Toast.makeText(applicationContext, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Inicia la actividad del LOGIN
     */
    private fun initLogin() {
        loadingView.dismiss()
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
    }

    /**
     * Registramos el usuario mediante retrofit en la
     * bbdd de nuestra API
     */
    private fun insertUserApi(userApi: UserApi) {
        val dto = UserMapper.toDTO(userApi)
        val call = bbddRest.insertUser(dto)

        call.enqueue((object : retrofit2.Callback<UserDTO> {
            override fun onResponse(call: retrofit2.Call<UserDTO>, response: retrofit2.Response<UserDTO>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.userSignUp),
                        Toast.LENGTH_SHORT
                    ).show()

                    Thread.sleep(500)

                    initLogin()

                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.userNoSignUp),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            //Si error
            override fun onFailure(call: retrofit2.Call<UserDTO>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.userNoSignUp),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }))

    }

    //Comprobar campos vaios
    private fun comprobarVacios(): Boolean {
        return Utilities.validarEmail(
            txtEmail,
            this
        ) and txtName.text.isNotEmpty() and txtPass.text.isNotEmpty() and txtNameUser.text.isNotEmpty()
    }

    //Dialog para camara o galeria
    private fun abrirOpciones() {
        imaUser.setOnClickListener {
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
    private fun abrirCamara() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            val permisosCamara =
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permisosCamara, CAMARA)
        } else {
            mostrarCamara()
        }
    }

    //abre la camara y hace la foto
    private fun mostrarCamara() {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        foto = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, foto)
        startActivityForResult(intent, CAMARA)
    }

    //pido los permisos para abrir la galeria
    private fun abrirGaleria() {
        val permiso = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissions(permiso, GALERIA)
    }


    //obtengo el resultado de pedir los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
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
        if (resultCode == Activity.RESULT_OK && requestCode == GALERIA) {
            imaUser.setImageURI(data?.data)
            Utilities.redondearFoto(imaUser)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            imaUser.setImageURI(foto)
            Utilities.redondearFoto(imaUser)
        }
    }

    //muetsro la galeria
    private fun mostrarGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALERIA)
    }

    //Guardamos datos
    private fun initSaveDatos() {
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
            email = getString("EMAIL").toString()
            nombre = getString("NOMBRE").toString()
            usuario = getString("USUARIO").toString()
            ima = Utilities.base64ToBitmap(getString("IMAGEN").toString())
            password = getString("PWD").toString()
        }
    }

}