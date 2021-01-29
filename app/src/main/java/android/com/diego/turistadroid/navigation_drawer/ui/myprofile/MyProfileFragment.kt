package android.com.diego.turistadroid.navigation_drawer.ui.myprofile

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.HttpClient
import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.utilities.Fotos
import android.com.diego.turistadroid.utilities.Utilities
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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.layout_seleccion_camara.view.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class MyProfileFragment(
    private val userApi: UserApi
) : Fragment() {

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

    private lateinit var clientImgur: OkHttpClient
    private lateinit var bbddRest: BBDDRest



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

        init()

        return root
    }

    private fun init(){

        initClients()
        asignarDatosUsuario()
        abrirRedes()

    }

    //clientes para las conexiones con las API de las que consumimos datos
    private fun initClients() {

        clientImgur = HttpClient.getClient()!!
        bbddRest = BBDDApi.service
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
        val str = "https://www.instagram.com/" + this.userApi.insta
        val uri = Uri.parse(str)
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(intent)
    }

    //Abrimos twitter del perfil del usuario
    private fun onClickTwitter(){
        val str = "https://www.twitter.com/" + this.userApi.twitter
        val uri = Uri.parse(str)
        val intent = Intent(Intent.ACTION_VIEW,uri)
        startActivity(intent)
    }

    //Asignamos a los componentes de la interfaz los datos del usuario logeado
    private fun asignarDatosUsuario(){
        Glide.with(this)
            .load(this.userApi.foto)
            .circleCrop()
            .into(imaProfile)
        txtNameProfile.text = this.userApi.name
        txtNameUserProfile.text = this.userApi.userName
        txtEmailProfile.setText(this.userApi.email)
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

    /**
     * Subimos la imagen que ha elegido el usuario a la
     * Api de IMGUR y creamos el usuario que posteriormente
     * registramos en la bbdd de nuestra API
     */
    private fun uploadImgToImgurAPI(imaString : String) {

        val mediaType: MediaType = "text/plain".toMediaTypeOrNull()!!
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", imaString)
            .build()
        val request = ImgurREST.postImage(body, "base64")
        clientImgur.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Toast.makeText(context!!, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (response.isSuccessful) {

                    val data = JSONObject(response.body!!.string())
                    val item = data.getJSONObject("data")
                    Log.i("imgur", item.getString("link"))
                    actualizarUsuario(item.getString("link"))

                } else {
                    Toast.makeText(context!!, getString(R.string.errorService), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun actualizarUsuario(foto : String?){
        val email = txtEmailProfile.text.toString()
        val name = txtNameProfile.text.toString()
        val nameUser = txtNameUserProfile.text.toString()
        val pass = Utilities.hashString(txtPassProfile.text.toString())

        Log.i("imgur", "$foto estoy en actualizar")

        val newUser = if (passChanged()) {
            UserApi(this.userApi.id, name, nameUser, email, pass, this.userApi.insta, this.userApi.twitter, foto)
        }else{
            UserApi(this.userApi.id, name, nameUser, email, this.userApi.pwd, this.userApi.insta, this.userApi.twitter, foto)
        }
        val newUserDTO = UserMapper.toDTO(newUser)
        actualizarUsuarioRemoto(newUserDTO)
        asignarDatosNavigation(newUser)
    }

    //Actualizamos el usuario con los datos nuevos en la BD
    private fun listenerChangedPhoto(){

        val imaStr = if (this::FOTO.isInitialized){
            Utilities.bitmapToBase64(this.FOTO)!!
        }else{
            this.userApi.foto
        }
        if (!imaStr.equals(this.userApi.foto)){
            uploadImgToImgurAPI(imaStr!!)
        }else{
            actualizarUsuario(this.userApi.foto)
        }
    }

    private fun actualizarUsuarioRemoto(newUserApi: UserDTO){

        val call = bbddRest.updateUser(this.userApi.id!!, newUserApi)

        call.enqueue(object : Callback<UserDTO>{
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful){
                    context!!.toast(R.string.newUserProfile)
                }else{
                    context!!.toast(R.string.errorNewUser)
                }
            }
            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                context!!.toast(R.string.errorService)
            }
        })
    }

    //Modificamos los datos del navigation drawer
    private fun asignarDatosNavigation(newUserApi: UserApi){
        NavigationDrawer.txtNombreNav.text = newUserApi.name
        NavigationDrawer.txtCorreoNav.text = newUserApi.email
        Glide.with(this)
            .load(newUserApi.foto)
            .circleCrop()
            .into(NavigationDrawer.imaUser_nav)
    }

    //Devuelve true si la pass ha sido modificada
    private fun passChanged(): Boolean{
        var cambiada = false

        if (txtPassProfile.text.isNotEmpty()){
            cambiada = true
        }
        return cambiada
    }

    /**
     * Comprobamos que el email no exista en la bbddd
     * tras haber hecho la comprobación de que el userName
     * sea unico
     */
    private fun uniqueEmail(email : String){
        val call = bbddRest.selectUserByEmail(email)

        call.enqueue((object : Callback<List<UserDTO>> {
            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {

                    //Si el body no esta vacio ese email ya esta registrado
                    if(response.body()!!.isNotEmpty()){
                        txtEmail.error = getString(R.string.errorEmail)
                    }else{ //en caso contrario no existe y permitimos el registro en la bbdd
                        listenerChangedPhoto()
                    }
                } else {
                    Toast.makeText(context!!, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            //Si error
            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(context!!, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }
        }))
    }

    /**
     * Comprobamos que el userName que introduce
     * el usuario no exista en nuestra bbdd
     */
    private fun uniqueUser(nameUser: String, email : String) {

        val call = bbddRest.selectUserByUserName(nameUser)

        call.enqueue((object : Callback<List<UserDTO>> {
            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {
                    //Si el cuerpo no esta vacio el usuario existe mostramos error
                    if(response.body()!!.isNotEmpty()){
                        txtNameUser.error = getString(R.string.errorNameUser)
                    }else{
                        if (email != userApi.email){ //Si quiere cambiar su email actual
                            uniqueEmail(email) //Si no es que no existe procedemos a comprobar el email
                        }else{
                            listenerChangedPhoto()
                        }
                    }
                } else {
                    Toast.makeText(context!!, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            //Si error
            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(context!!, getString(R.string.errorUpload), Toast.LENGTH_SHORT)
                    .show()
            }
        }))
    }

    //Comprobamos que no haya campos vacios y el usuario sea unico
    private fun checkUsuario(){
        btnSave.setOnClickListener {
            Log.i("valor de vacios",comprobarVacios().toString())
            if(comprobarVacios()){
                when {
                    txtNameUserProfile.text.toString() != userApi.userName -> {
                        uniqueUser(txtNameUserProfile.text.toString(), txtEmailProfile.text.toString())
                    }
                    txtEmailProfile.text.toString() != userApi.email -> {
                        uniqueEmail(txtEmailProfile.text.toString())
                    }
                    else -> {
                        listenerChangedPhoto()
                    }
                }
            }else{
                Toast.makeText(context, getString(R.string.action_emptyfield), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //devuelve true si el campo del email ha sido modificado
    private fun checkEmailChange(): Boolean{
        var v = false
        if(this.userApi.email != txtEmailProfile.text.toString()){
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