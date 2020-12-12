package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.*
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.signup.SignUp
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    private var userSave = ""
    private var pwdSave = ""
    private var sesion = Session()
    companion object{
        var user = User()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_log_in)
        txtUser_Login.setText("d@d.d")
        txtPwd_Login.setText("d")
        clickBtn()
        clickRegister()
        //sdf()

    }

    private fun sdf(){
        txtUser_Login.setText("x@x.c")
        txtPwd_Login.setText("x")
    }

    override fun onStop() {
        super.onStop()
        ControllerBbdd.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        ControllerBbdd.close()
    }

    private fun comprobarLogin(email : String, pwd : String) : Boolean{
    try{
        user = ControllerUser.selectByEmail(email)!!
    }catch (e: IllegalArgumentException){
    }
        return pwd == user.pwd

    }


    private fun clickBtn(){

        btnLogIn.setOnClickListener {

            val email = txtUser_Login.text.toString()
            val pwd = Utilities.hashString(txtPwd_Login.text.toString())

            if (email.isEmpty()) {
                txtUser_Login.error = getString(R.string.action_emptyfield)
            }
            if (pwd.isEmpty()) {
                txtPwd_Login.error = getString(R.string.action_emptyfield)
            }

            if (email.isNotEmpty() and pwd.isNotEmpty()) {

                if (comprobarLogin(email, pwd)) {
                    if (comprobarRed()) {
                        insertarSession(email)
                        initNavigation()
                    }
                }else { txtUser_Login.error = getString(R.string.errorLogin) }
            }
        }
    }

    private fun comprobarRed(): Boolean {

        var red = false
        if (Utilities.isNetworkAvailable(applicationContext)) {
            Toast.makeText(applicationContext, getString(R.string.InternetON), Toast.LENGTH_SHORT)
                .show()
            red = true
        } else {
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

    
    private fun insertarSession(email: String){
        sesion = Session(email)
        ControllerSession.insertSession(sesion)
    }

    private fun clickRegister(){
        textRegister_LogIn.setOnClickListener {
            initRegister()
        }
    }

    private fun initRegister(){

        val intent = Intent (this, SignUp::class.java)
        startActivity(intent)
    }

    private fun initNavigation(){

        val intent = Intent (this, NavigationDrawer::class.java)
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