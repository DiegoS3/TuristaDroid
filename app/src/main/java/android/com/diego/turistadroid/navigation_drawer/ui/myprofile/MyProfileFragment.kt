package android.com.diego.turistadroid.navigation_drawer.ui.myprofile

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_CANCELED
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerSession
import android.com.diego.turistadroid.bbdd.ControllerUser
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.splash.SplashScreenActivity
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import java.io.IOException


class MyProfileFragment(
    private val userApi: UserApi
) : Fragment() {

    private lateinit var user : User
    // Variables para la camara
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var IMAGEN_NOMBRE: String
    private lateinit var IMAGEN_URI: Uri
    private val IMAGEN_DIR = "/TuristaDroid"
    private val IMAGEN_PROPORCION = 600
    private lateinit var FOTO: Bitmap
    private var IMAGEN_COMPRES = 80

    private lateinit var imaProfile: ImageView
    private lateinit var txtNameProfile: TextView
    private lateinit var txtNameUserProfile: TextView
    private lateinit var txtEmailProfile: EditText
    private lateinit var txtPassProfile: EditText

    private lateinit var imaInstagram: ImageView
    private lateinit var imaTwitter: ImageView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        //val textView: TextView = root.findViewById(R.string.myProfileTitle)

        imaProfile = root.findViewById(R.id.imaProfile)
        txtNameProfile= root.findViewById(R.id.txtUserName)
        txtNameUserProfile = root.findViewById(R.id.txtNameUserProfile)
        txtEmailProfile = root.findViewById(R.id.txtEmailProfile)
        txtPassProfile= root.findViewById(R.id.txtPassProfile)

        imaInstagram = root.findViewById(R.id.imaInstagram)
        imaTwitter = root.findViewById(R.id.imaTwitter)
        userSwitch()
        asignarDatosUsuario()
        abrirRedes()

        return root
    }

    //Usuario segun hayamos entrado a la aplicacion
    private fun userSwitch(){
        user = if(SplashScreenActivity.login) {
            LogInActivity.user
        }else{
            val listaSesion = ControllerSession.selectSessions()!!
            val emailSesion = listaSesion[0].emailUser
            ControllerUser.selectByEmail(emailSesion)!!
        }
    }
    //Abrir redes sociales al hacer click en su boton correspondiente
    private fun abrirRedes(){
        imaInstagram.setOnClickListener {
            onClickInstagram()
        }
        imaTwitter.setOnClickListener {
            onClickTwitter()
        }
    }

    //Abrimos instagram del perfil del usuario
    private fun onClickInstagram(){
        val str = "https://www.instagram.com/"+user.instagram
        Log.i("instagram: ", user.instagram)

        Toast.makeText(context, "instagram: "+user.instagram, Toast.LENGTH_SHORT).show()
        val uri = Uri.parse(str)
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(intent)
    }

    //Abrimos twitter del perfil del usuario
    private fun onClickTwitter(){
        val str = "https://www.twitter.com/"+user.twitter
        Log.i("twitter: ", user.twitter)
        Toast.makeText(context, "twitter: "+user.twitter, Toast.LENGTH_SHORT).show()
        val uri = Uri.parse(str)
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(intent)
    }

    //Asignamos a los componentes de la interfaz los datos del usuario logeado
    private fun asignarDatosUsuario(){
        imaProfile.setImageBitmap(Utilities.base64ToBitmap(user.foto))
        Utilities.redondearFoto(imaProfile)
        txtNameProfile.text = user.nombre
        txtNameUserProfile.text = user.nombreUser
        txtEmailProfile.setText(user.email)
    }

    //Opciones para insertar foto (camara o galeria)
    private fun abrirOpciones() {
        imaProfile.setOnClickListener(){
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
        if (resultCode == RESULT_CANCELED) {
        }
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
                    imaProfile.setImageBitmap(this.FOTO)
                    Utilities.redondearFoto(imaProfile)//Redondea la foto
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
        Utilities.validarEmail(txtEmailProfile, context!!)
        checkUsuario()
    }


    //Actualizamos el usuario con los datos nuevos en la BD
    private fun actualizarUsuario(){
        val email = txtEmailProfile.text.toString()
        val name = txtNameProfile.text.toString()
        val nameUser = txtNameUserProfile.text.toString()
        val pass = Utilities.hashString(txtPassProfile.text.toString())
        val imaStr = if (this::FOTO.isInitialized){
            Utilities.bitmapToBase64(this.FOTO)!!
        }else{
            user.foto
        }
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

    //Modificamos los datos del navigation drawer
    private fun asignarDatosNavigation(){
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

    //Eliminar usuaro BD
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

    //Comprobamos que no haya campos vacios y el usuario sea unico
    private fun checkUsuario(){
        btnSave.setOnClickListener {
            Log.i("valor de vacios",comprobarVacios().toString())
            if(comprobarVacios()){
                try {
                    if(ControllerUser.uniqueUser(txtNameUserProfile.text.toString())){
                        if(txtNameUserProfile.text.toString() == user.nombreUser){
                            actualizarUsuario()
                        }else {
                            txtNameUserProfile.error = getString(R.string.errorNameUser)
                        }
                    }else{
                        actualizarUsuario()
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
        if  (checkEmailChange()){
            if (Utilities.validarEmail(txtEmailProfile, context!!) and txtNameProfile.text.isNotEmpty() and txtNameUserProfile.text.isNotEmpty()){
                valido = true
            }
        }else{
            if (txtNameProfile.text.isNotEmpty() and txtNameUserProfile.text.isNotEmpty()){
                valido = true
            }
        }
        return valido
    }
}