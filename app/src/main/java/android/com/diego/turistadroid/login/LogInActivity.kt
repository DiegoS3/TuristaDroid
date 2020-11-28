package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.MainActivity
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerBbdd
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    private var userSave = ""
    private var pwdSave = ""
    companion object{
        var user = User()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultamos la barra de action
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_log_in)

        clickBtn()

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

        user = ControllerBbdd.selectByEmail(email)!!

        return pwd == user.pwd

    }

    private fun clickBtn(){

        btnLogIn.setOnClickListener {

            val email = txtUser_Login.text.toString()
            val pwd = Utilities.hashString(txtPwd_Login.text.toString())

            if (email.isEmpty()){ txtUser_Login.error = R.string.action_emptyfield.toString() }
            if (pwd.isEmpty()){ txtPwd_Login.error = R.string.action_emptyfield.toString()}

            if (email.isNotEmpty() and pwd.isNotEmpty()){

                if (comprobarLogin(email, pwd)){ initNavigation() }
                else { txtUser_Login.error = R.string.errorLogin.toString() }

            }
        }
    }

    private fun initNavigation(){

        val intent = Intent (this, MainActivity::class.java)
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