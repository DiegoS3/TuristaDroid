package android.com.diego.turistadroid.navigation_drawer.ui.myprofile

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.R.attr.bitmap
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.firebase.UserFB
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.getImageUri
import android.com.diego.turistadroid.utilities.Utilities.toast
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.core.util.ObjectsCompat.equals
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException
import java.net.URI
import java.util.Objects.equals


class MyProfileFragment(
    private val userFB: UserFB
): Fragment() {

    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var IMAGEN_NOMBRE: String
    private lateinit var IMAGEN_URI: Uri
    private val IMAGEN_DIR = "/TuristaDroid"
    private val IMAGEN_PROPORCION = 600
    private lateinit var FOTO: Bitmap
    private var IMAGEN_COMPRES = 80
    private lateinit var url: String

    private lateinit var imaProfile: ImageView
    private lateinit var txtNameProfile: TextView
    private lateinit var txtNameUserProfile: TextView
    private lateinit var txtEmailProfile: EditText
    private lateinit var txtPassProfile: EditText

    private lateinit var imaInstagram: ImageView
    private lateinit var imaTwitter: ImageView

    //Vars Firebase
    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

    private lateinit var storage: FirebaseStorage
    private lateinit var storage_ref: StorageReference
    private var urlImage = ""
    private lateinit var user: FirebaseUser
    private var fotoCombiada = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        //val textView: TextView = root.findViewById(R.string.myProfileTitle)

        imaProfile = root.findViewById(R.id.imaProfile)
        txtNameProfile = root.findViewById(R.id.txtUserName)
        txtNameUserProfile = root.findViewById(R.id.txtNameUserProfile)
        txtEmailProfile = root.findViewById(R.id.txtEmailProfile)
        txtPassProfile = root.findViewById(R.id.txtPassProfile)

        imaInstagram = root.findViewById(R.id.imaInstagram)
        imaTwitter = root.findViewById(R.id.imaTwitter)

        init()

        return root
    }

    private fun init() {
        initFirebase()
        getCurrentUser()
        asignarDatosUsuario()
        abrirRedes()

    }

    private fun getCurrentUser() {
        user = Auth.currentUser!!
    }

    private fun initFirebase() {
        storage = Firebase.storage("gs://turistadroid.appspot.com/")
        storage_ref = storage.reference
        FireStore = FirebaseFirestore.getInstance()
        Auth = Firebase.auth
    }


    //Abrir redes sociales al hacer click en su boton correspondiente
    private fun abrirRedes() {
        imaInstagram.setOnClickListener {
            onClickInstagram()
        }
        imaTwitter.setOnClickListener {
            onClickTwitter()
        }
    }

    //Abrimos instagram del perfil del usuario
    private fun onClickInstagram() {
        //val str = "https://www.instagram.com/" + this.user.insta
        //val uri = Uri.parse(str)
        //val intent = Intent(Intent.ACTION_VIEW, uri)
        //startActivity(intent)
    }

    //Abrimos twitter del perfil del usuario
    private fun onClickTwitter() {
        //val str = "https://www.twitter.com/" + this.userApi.twitter
        //val uri = Uri.parse(str)
        //val intent = Intent(Intent.ACTION_VIEW, uri)
        //startActivity(intent)
    }

    //Asignamos a los componentes de la interfaz los datos del usuario logeado
    private fun asignarDatosUsuario() {
        Glide.with(this)
            .load(user.photoUrl)
            .circleCrop()
            .into(imaProfile)
        txtNameProfile.text = this.user.displayName
        //txtNameUserProfile.text = this.user.
        txtEmailProfile.setText(user.email)
    }

    //Opciones para insertar foto (camara o galeria)
    private fun abrirOpciones() {
        imaProfile.setOnClickListener() {
            val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(context!!)
                .setView(mDialogView).create()
            mBuilder.show()

            //Listener para abrir la camara
            mDialogView.txtCamara.setOnClickListener {
                abrirCamara()
                mBuilder.dismiss()
            }

            //Listener para abrir la galeria
            mDialogView.txtGaleria.setOnClickListener {
                elegirFotoGaleria()
                mBuilder.dismiss()
            }
        }
    }

    /**
     * Elige una foto de la galeria
     */
    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    //Llamamos al intent de la camara
    private fun tomarFotoCamara() {
        // Si queremos hacer uso de fotos en alta calidad
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Eso para alta o baja
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = Fotos.crearNombreFoto("camara", ".jpg")
        // Salvamos el fichero
        val fichero = Fotos.salvarFoto(IMAGEN_DIR, IMAGEN_NOMBRE, context!!)
        IMAGEN_URI = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN_URI)
        // Esto para alta y baja
        startActivityForResult(intent, CAMARA)
    }

    /**
     * Siempre se ejecuta al realizar una acción
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Procesamos la foto de la galeria
        if (requestCode == GALERIA) {
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI);
                    } else {
                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context?.contentResolver!!, contentURI)
                        this.FOTO = ImageDecoder.decodeBitmap(source)
                    }
                    // Para jugar con las proporciones
                    val prop = this.IMAGEN_PROPORCION / this.FOTO.width.toFloat()
                    // Actualizamos el bitmap para ese tamaño
                    this.FOTO = Bitmap.createScaledBitmap(
                        this.FOTO,
                        this.IMAGEN_PROPORCION,
                        (this.FOTO.height * prop).toInt(),
                        false
                    )
                    fotoCombiada = true
                    imaProfile.setImageBitmap(this.FOTO)
                    Utilities.redondearFoto(imaProfile)


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMARA) {
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail)
            try {

                if (Build.VERSION.SDK_INT < 28) {
                    this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, IMAGEN_URI)
                    this.FOTO = ImageDecoder.decodeBitmap(source)
                }
                fotoCombiada = true

                //omprimir imagen
                Fotos.comprimirImagen(IMAGEN_URI.toFile(), this.FOTO, this.IMAGEN_COMPRES)
                IMAGEN_URI = Fotos.añadirFotoGaleria(IMAGEN_URI, IMAGEN_NOMBRE, context!!)!!

                // Mostramos
                imaProfile.setImageBitmap(this.FOTO)
                Utilities.redondearFoto(imaProfile)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //muestro la camara
    private fun abrirCamara() =
        if (ActivityCompat.checkSelfPermission(context!!, CAMERA) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            val permisosCamara =
                arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
            requestPermissions(permisosCamara, CAMARA)
        } else {
            tomarFotoCamara()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        abrirOpciones()
        Utilities.validarPassword(txtPassProfile, progressBar_MyProfile, password_strength_MyProfile, context!!)
        Utilities.validarEmail(txtEmailProfile, context!!)
        checkUsuario()
    }


    private fun actualizarUsuario() {
        val email = txtEmailProfile.text.toString()
        val name = txtNameProfile.text.toString()
        val nameUser = txtNameUserProfile.text.toString()
        val pass = Utilities.hashString(txtPassProfile.text.toString())

        val newUser = if (passChanged()) {
            UserFB(user.uid, name, nameUser, email, pass, this.userFB.insta, this.userFB.twitter, user.photoUrl.toString())
        } else {
            UserFB(
                user.uid,
                name,
                nameUser,
                email,
                this.userFB.pwd,
                this.userFB.insta,
                this.userFB.twitter,
                user.photoUrl.toString()
            )
        }
        actualizarBD(newUser)

    }

    private fun actualizarBD(newUserFB: UserFB) {
        FireStore.collection("users")
            .document(userFB.id!!)
            .set(newUserFB)
            .addOnCompleteListener{ task->
                if (task.isSuccessful){
                    context!!.toast(R.string.newUserProfile)
                }else{
                    context!!.toast(R.string.errorService)
                }
            }
    }

    private fun asignarDatosUsuario(newUserApi: UserApi) {

        Thread {
            try {
                Thread.sleep(100)

            } catch (e: InterruptedException) {
            }
            activity!!.runOnUiThread {
                NavigationDrawer.txtNombreNav.text = newUserApi.name
                NavigationDrawer.txtCorreoNav.text = newUserApi.email
                Glide.with(context!!)
                    .asBitmap()
                    .load(newUserApi.foto)
                    .circleCrop()
                    .into(BitmapImageViewTarget(NavigationDrawer.imaUser_nav))
            }
        }.start()


    }


    //Devuelve true si la pass ha sido modificada
    private fun passChanged(): Boolean {
        var cambiada = false

        if (txtPassProfile.text.isNotEmpty()) {
            cambiada = true
        }
        return cambiada
    }


    /**
     * Comprobamos que el userName que introduce
     * el usuario no exista en nuestra bbdd
     */
    private fun uniqueUser(nameUser: String, email: String) {
        FireStore.collection("users")
            .whereEqualTo("userName", txtNameUser.text.toString())
            .get()
            .addOnSuccessListener {
                Log.i("fire", it.documents.size.toString())
                if (it.documents.size == 0) {

                } else {
                    context!!.toast(R.string.errorNameUser)
                }
            }
            .addOnFailureListener {
                context!!.toast(R.string.errorService)
            }

    }

    //Comprobamos que no haya campos vacios y el usuario sea unico
    private fun checkUsuario() {
        btnSave.setOnClickListener {
            if (txtNameProfile.text.isNotEmpty()) {
                if (fotoCombiada || txtNameProfile.text != user.displayName ){
                    actualizarUserDatos()
                }
                else{
                    actualizarUsuario()
                }

            } else {
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarUserDatos() {
        val profileUpdates = userProfileChangeRequest {
            displayName = txtNameProfile.text.toString()
            photoUri = Uri.parse(urlImage)
        }
        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                actualizarUsuario()
            }
        }
    }

}