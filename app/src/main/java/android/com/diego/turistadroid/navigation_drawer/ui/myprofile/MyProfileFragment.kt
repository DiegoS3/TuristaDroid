package android.com.diego.turistadroid.navigation_drawer.ui.myprofile

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_CANCELED
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerUser
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException


class MyProfileFragment : Fragment() {

    private lateinit var myProfileViewModel: MyProfileViewModel
    private var user = LogInActivity.user
    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var IMAGEN_NOMBRE: String
    private lateinit var IMAGEN_URI: Uri
    private val IMAGEN_DIR = "/MisLugares"
    private val IMAGEN_PROPORCION = 600
    private lateinit var FOTO: Bitmap
    private var IMAGEN_COMPRES = 80

    private lateinit var imaProfile: ImageView
    private lateinit var txtNameProfile: TextView
    private lateinit var txtNameUserProfile: TextView
    private lateinit var txtEmailProfile: EditText
    private lateinit var txtPassProfile: EditText



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myProfileViewModel =
            ViewModelProviders.of(this).get(MyProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        //val textView: TextView = root.findViewById(R.string.myProfileTitle)

        imaProfile = root.findViewById(R.id.imaProfile)
        txtNameProfile= root.findViewById(R.id.txtNameProfile)
        txtNameUserProfile = root.findViewById(R.id.txtNameUserProfile)
        txtEmailProfile = root.findViewById(R.id.txtEmailProfile)
        txtPassProfile= root.findViewById(R.id.txtPassProfile)
        asignarDatosUsuario()

        myProfileViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        return root
    }

    private fun asignarDatosUsuario(){
        imaProfile.setImageBitmap(Utilities.base64ToBitmap(user.foto))
        Utilities.redondearFoto(imaProfile)
        txtNameProfile.text = user.nombre
        txtNameUserProfile.text = user.nombreUser
        txtEmailProfile.setText(user.email)
    }



    private fun abrirOpciones() {
        imaProfile.setOnClickListener(){
            val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.layout_seleccion_camara, null)
            val mBuilder = AlertDialog.Builder(context!!)
                .setView(mDialogView).create()
            val mAlertDialog = mBuilder.show()

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
    // https://developer.android.com/training/camera/photobasics.html#TaskPath
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
        Log.i("FOTO", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            Log.i("FOTO", "Se ha cancelado")
        }
        // Procesamos la foto de la galeria
        if (requestCode == GALERIA) {
            Log.i("FOTO", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    // Dependeindo de la versión del SDK debemos hacerlo de una manera u otra
                    if (Build.VERSION.SDK_INT < 28) {
                        this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI);
                    } else {
                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context?.contentResolver!!, contentURI)
                        this.FOTO = ImageDecoder.decodeBitmap(source)
                    }
                    // Para jugar con las proporciones y ahorrar en memoria no cargando toda la foto, solo carga 600px max
                    val prop = this.IMAGEN_PROPORCION / this.FOTO.width.toFloat()
                    // Actualizamos el bitmap para ese tamaño, luego podríamos reducir su calidad
                    this.FOTO = Bitmap.createScaledBitmap(
                        this.FOTO,
                        this.IMAGEN_PROPORCION,
                        (this.FOTO.height * prop).toInt(),
                        false
                    )
                    Toast.makeText(context, "¡Foto rescatada de la galería!", Toast.LENGTH_SHORT).show()
                    imaProfile.setImageBitmap(this.FOTO)
                    Utilities.redondearFoto(imaProfile)
                    // Vamos a copiar nuestra imagen en nuestro directorio
                    // Utilidades.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, applicationContext)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMARA) {
            Log.i("FOTO", "Entramos en Camara")
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail)
            try {
                // Esta línea para baja calidad
                //thumbnail = (Bitmap) data.getExtras().get("data");
                // Esto para alta
                //val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, IMAGEN_URI)
                //val foto: Bitmap = ImageDecoder.decodeBitmap(source)

                if (Build.VERSION.SDK_INT < 28) {
                    this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, IMAGEN_URI)
                    this.FOTO = ImageDecoder.decodeBitmap(source)
                }

                // Vamos a probar a comprimir
                Fotos.comprimirImagen(IMAGEN_URI.toFile(), this.FOTO, this.IMAGEN_COMPRES)

                // Si estamos en módo publico la añadimos en la biblioteca
                // if (PUBLICO) {
                // Por su queemos guardar el URI con la que se almacena en la Mediastore
                IMAGEN_URI = Fotos.añadirFotoGaleria(IMAGEN_URI, IMAGEN_NOMBRE, context!!)!!
                // }

                // Mostramos
                imaProfile.setImageBitmap(this.FOTO)
                Utilities.redondearFoto(imaProfile)
                Toast.makeText(context, "¡Foto Salvada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Camara!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //muestro la camara
    private fun abrirCamara() = if (ActivityCompat.checkSelfPermission(context!!, CAMERA) == PackageManager.PERMISSION_DENIED ||
        ActivityCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
        val permisosCamara =
            arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
        requestPermissions(permisosCamara, CAMARA)
    }else{
        tomarFotoCamara()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        abrirOpciones()
        Utilities.validarPassword(txtPassProfile, progressBar_MyProfile, password_strength_MyProfile, context!!)
        Utilities.validarEmail(txtEmailProfile)
        checkUsuario()
    }


    private fun actualizarUsuario(){
        val email = txtEmailProfile.text.toString()
        val name = txtNameProfile.text.toString()
        val nameUser = txtNameUserProfile.text.toString()
        val pass = Utilities.hashString(txtPassProfile.text.toString())
        val imaStr = Utilities.bitmapToBase64(this.FOTO)!!
        val listaSitiosUsuario = user.places

        eliminarUser()

        val newUser = if (passChanged()) {
             User(email, name, nameUser, pass, imaStr, listaSitiosUsuario)
        }else{
            User(email, name, nameUser, user.pwd, imaStr, listaSitiosUsuario)
        }
        ControllerUser.insertUser(newUser)
        user = newUser
        LogInActivity.user = newUser
        asignarDatosNavigation()
    }

    fun asignarDatosNavigation(){
        user = LogInActivity.user
        NavigationDrawer.txtNombreNav.text = user.nombre
        NavigationDrawer.txtCorreoNav.text = user.email

        if (user.foto != ""){
            NavigationDrawer.imaUser_nav.setImageBitmap(Utilities.base64ToBitmap(user.foto))
            Utilities.redondearFoto(NavigationDrawer.imaUser_nav)
        }else{
            NavigationDrawer.imaUser_nav.setImageResource(R.drawable.ima_user)
        }
    }

    private fun eliminarUser(){
        ControllerUser.deleteUser(user.email)
    }

    //Devuelve true si la pass ha sido modificada
    private fun passChanged(): Boolean{
        var cambiada = false

        if (txtPassProfile.text.isNotEmpty()){
            cambiada = true
        }
        return cambiada
    }


    private fun checkUsuario(){
        btnSave.setOnClickListener {
            Log.i("valor de vacios",comprobarVacios().toString())
            if(comprobarVacios()){
                try {

                    if(ControllerUser.uniqueUser(txtNameUserProfile.text.toString())){
                        if(txtNameUserProfile.text.toString() == user.nombreUser){
                            actualizarUsuario()
                            Toast.makeText(context, "Usuario actualizado con exito", Toast.LENGTH_SHORT).show()
                        }else {
                            txtNameUserProfile.error = getString(R.string.errorNameUser)
                        }
                    }else{
                        actualizarUsuario()
                        Toast.makeText(context, "Usuario actualizado con exito", Toast.LENGTH_SHORT).show()
                    }

                }catch (ex: RealmPrimaryKeyConstraintException){
                    txtEmailProfile.error = getString(R.string.errorEmail)
                }
            }else{
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }


    //devuelve true si el campo del email ha sido modificado
    private fun checkEmailChange(): Boolean{
        var v = false
        if(user.email != txtEmailProfile.text.toString()){
            v = true
        }
        return v
    }


    private fun comprobarVacios(): Boolean{
        var valido = false
        Log.i("validar email:", Utilities.validarEmail(txtEmailProfile).toString())
        Log.i("nameProfile:", txtNameProfile.text.isNotEmpty().toString())
        Log.i("nameUserProfile:", txtNameUserProfile.text.isNotEmpty().toString())
        Log.i("txtNameProfile:", txtNameProfile.text.toString())
        Log.i("txtNameUserProfile:", txtNameUserProfile.text.toString())
        Log.i("txtEmailProfile:", txtEmailProfile.text.toString())
        if  (checkEmailChange()){
            if (Utilities.validarEmail(txtEmailProfile) and txtNameProfile.text.isNotEmpty() and txtNameUserProfile.text.isNotEmpty()){
                valido = true
            }
        }else{
            if (txtNameProfile.text.isNotEmpty() and txtNameUserProfile.text.isNotEmpty()){
                valido = true
            }
        }
        return valido
    }

    override fun onResume() {
        super.onResume()
        //asignarDatosUsuario()
    }






}