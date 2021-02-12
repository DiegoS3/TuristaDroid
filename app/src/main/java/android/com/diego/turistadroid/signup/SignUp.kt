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
import android.com.diego.turistadroid.bbdd.firebase.UserFB
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.toast
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
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.layout_input_instagram.view.*
import kotlinx.android.synthetic.main.layout_input_twitter.view.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

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

    private lateinit var loadingView: AlertDialog

    //Vars Firebase
    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

    private lateinit var storage: FirebaseStorage
    private lateinit var storage_ref: StorageReference
    private var urlImage = ""
    private lateinit var user : FirebaseUser

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
        initDialog()
        redes()
        checkUsuario()
        initFirebase()
    }

    private fun initDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        loadingView = builder.create()
    }

    private fun initFirebase() {
        storage = Firebase.storage("gs://turistadroid.appspot.com/")
        storage_ref = storage.reference
        FireStore = FirebaseFirestore.getInstance()
        Auth = Firebase.auth
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
     * Comprobamos que el userName que introduce
     * el usuario no exista en nuestra bbdd
     */
    private fun uniqueUser() {
        FireStore.collection("users")
            .whereEqualTo("userName", txtNameUser.text.toString())
            .get()
            .addOnSuccessListener {
                Log.i("fire", it.documents.size.toString())
                if (it.documents.size == 0) {
                    loadingView.show()
                    crearUser()
                } else {
                    applicationContext.toast(R.string.errorNameUser)
                }
            }
            .addOnFailureListener {
                applicationContext.toast(R.string.errorService)
            }
    }

    //Comprobar suario
    private fun checkUsuario() {
        btnRegister.setOnClickListener {

            if (comprobarVacios()) {
                uniqueUser()
            } else {
                Toast.makeText(applicationContext, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearUser() {
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        Auth.createUserWithEmailAndPassword(txtEmail.text.toString(), passCifrada)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = Auth.currentUser!!
                    uploadFotoStorage()
                } else {

                    if (task.exception is FirebaseAuthUserCollisionException)
                        applicationContext.toast(R.string.errorEmail)
                    else if (task.exception is FirebaseAuthInvalidCredentialsException)
                        applicationContext.toast(R.string.errorEmailFormat)
                }
            }
    }

    private fun crearUserDatos() {
        val profileUpdates = userProfileChangeRequest {
            displayName = txtName.text.toString()
            photoUri = Uri.parse(urlImage)
        }
        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                insertUserFireBase()
            }
        }
    }

    /**
     * Inicia la actividad del LOGIN
     */
    private fun initLogin() {
        loadingView.dismiss()
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Registramos el usuario mediante Firebase en el
     * Cloud FireStore
     */
    private fun insertUserFireBase() {
        val passCifrada = Utilities.hashString(txtPass.text.toString())
        val userFB = UserFB(
            user.uid,
            user.displayName,
            txtNameUser.text.toString(),
            user.email,
            passCifrada,
            instagram,
            twitter,
            user.photoUrl.toString()
        )
        Log.i("fire", "antes de collection")
        FireStore.collection("users")
            .document(user.uid)
            .set(userFB)
            .addOnSuccessListener {
                applicationContext.toast(R.string.userSignUp)
                initLogin()
            }
            .addOnFailureListener {
                applicationContext.toast(R.string.userNoSignUp)
            }
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
            foto = data?.data
            Glide.with(this)
                .load(foto!!)
                .circleCrop()
                .into(imaUser)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMARA) {
            Glide.with(this)
                .load(foto!!)
                .circleCrop()
                .into(imaUser)
        }
    }

    /**
     * Subimos la imagen que ha elegido el usuario al
     * Storage de Firebase
     */
    private fun uploadFotoStorage() {
        val foto_ref = storage_ref.child("/avatares/${foto!!.lastPathSegment}")
        val uploadTask = foto_ref.putFile(foto!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            foto_ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.isComplete){
                    urlImage = task.result.toString()
                    crearUserDatos()
                    Log.i("task", task.result.toString())
                }

            }
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