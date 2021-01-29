package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.MyApplication
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.Session
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.Sessions
import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.SessionsDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.SessionsMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.signup.SignUp
import android.com.diego.turistadroid.utilities.UtilSessions
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_log_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogInActivity : AppCompatActivity() {

    //mis variables
    private var userSave = ""
    private var pwdSave = ""
    private lateinit var bbddRest: BBDDRest

    companion object {
        var user = User() //usuario que compartiremos con activities y fragments
    }

    //Cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_log_in)

        init()
    }

    private fun init(){
        bbddRest = BBDDApi.service
        clickBtn()
        clickRegister()
    }

    /**
     * Comprueba que los datos del usuario existen en la tabla
     *
     * @param email Email introducido por el usuario
     * @param pwd Contraseña introducida por el usuario
     *
     * @return true si el email y la pwd coinciden con los de la BD
     *         false en caso de que alguno de los dos no coincida
     *
     */
    private fun comprobarLogin(email: String, pwd: String){

        val call = bbddRest.selectUserByEmail(email)

        call.enqueue((object : Callback<List<UserDTO>> {
            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {
                    //Si el body no esta vacio ese email ya esta registrado
                    if(response.body()!!.isNotEmpty()){
                        val user = UserMapper.fromDTO(response.body()!![0])

                        if (user.pwd == pwd){
                            val id = user.id
                            (application as MyApplication).USUARIO_API = user
                            insertarSession(id)
                        }else{
                            txtPwd_Login.error = getString(R.string.errorLoginPWD)
                        }

                    }else{ //en caso contrario no existe
                        txtUser_Login.error = getString(R.string.errorLoginEmail) //en caso contraio seteamos el error
                    }
                } else {

                    Toast.makeText(applicationContext, getString(R.string.errorLogin), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            //Si error
            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.errorService), Toast.LENGTH_SHORT)
                    .show()
            }
        }))
    }

    /**
     * Metodo en el que al pulsar en login se comprueban los
     * diferentes casos que se pueden dar al intentar iniciar sesion
     */
    private fun clickBtn() {

        //Evento al hacer click
        btnLogIn.setOnClickListener {

            val email = txtUser_Login.text.toString()//email introducido en el EditText
            //pwd introducida en el EditText y convertida a Sha-256
            val pwd = Utilities.hashString(txtPwd_Login.text.toString())

            if (email.isEmpty()) { //En caso de estar el EditText del email vacio
                txtUser_Login.error = getString(R.string.action_emptyfield) //seteamos el error a mostrar
            }
            if (pwd.isEmpty()) { //En caso de estar el EditText del pwd vacio
                txtPwd_Login.error = getString(R.string.action_emptyfield) //seteamos el error a mostrar
            }

            // en caso de no estar ninguno de los EditText vacios
            if (email.isNotEmpty() and pwd.isNotEmpty()) {
                comprobarLogin(email, pwd)
            }
        }
    }

    /**
     * Metodo que comprueba si el usuario tiene activa la conexion a internet
     *
     * @return true en caso de tener activa la conexion
     *         false en caso de no tener activa la conexion
     */
    private fun comprobarRed(): Boolean {

        var red = false
        //Si esta activa
        if (Utilities.isNetworkAvailable(applicationContext)) { red = true}
        else { //si no esta activa
            //Mostramos snack bar que le permite acceder a las opciones para activar la conexion a internet
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.InternetNeed),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.colorAccent))
            snackbar.setAction(getString(R.string.Connect)) {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
        return red
    }

    /**
     * Metodo que inserta al usuario en la tabla de sesiones
     */
    private fun insertarSession(idUser: String?) {
        val currentDate = Utilities.dateToString(Utilities.getSysDate())!!
        val idSession = UUID.randomUUID().toString()
        val session = Sessions(idSession, idUser, currentDate)
        val sessionDTO = SessionsMapper.toDTO(session)
        val call = bbddRest.insertSession(sessionDTO)

        call.enqueue(object : Callback<SessionsDTO> {
            override fun onResponse(call: Call<SessionsDTO>, response: Response<SessionsDTO>) {

                if (response.isSuccessful){
                    UtilSessions.crearSesionLocal(session, currentDate, applicationContext)
                    initNavigation()
                }else{
                    Log.i("sesion", "Error al crear la sesion en el login")
                }
            }
            override fun onFailure(call: Call<SessionsDTO>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.errorService), Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    /**
     * Metodo que al hacer click en el text view Register
     * te lleva a la actividad que te permite registrarte
     */
    private fun clickRegister() {
        textRegister_LogIn.setOnClickListener {
            initRegister()
        }
    }

    /**
     * Metodo que contiene el intent que permite ir a la actividad SignUp
     */
    private fun initRegister() {

        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }

    /**
     * Metodo que contiene el intent que permite ir a la actividad NavigataionDrawer
     */
    private fun initNavigation() {

        val intent = Intent(this, NavigationDrawer::class.java)
        startActivity(intent)

    }

    // Para salvar el estado por ejemplo es usando un Bundle en el ciclo de vida
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("USER", userSave)
            putString("PWD", pwdSave)
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
            userSave = getString("USER").toString()
            pwdSave = getString("PWD").toString()

        }
    }

}