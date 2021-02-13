package android.com.diego.turistadroid.login

import android.com.diego.turistadroid.MyApplication
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDRest
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.com.diego.turistadroid.bbdd.realm.entities.User
import android.com.diego.turistadroid.navigation_drawer.NavigationDrawer
import android.com.diego.turistadroid.signup.SignUp
import android.com.diego.turistadroid.utilities.Utilities
import android.com.diego.turistadroid.utilities.Utilities.toast
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.services.AccountService
import kotlinx.android.synthetic.main.activity_log_in.*
import java.util.*


class LogInActivity : AppCompatActivity() {

    //mis variables
    private var userSave = ""
    private var pwdSave = ""
    private lateinit var bbddRest: BBDDRest
    private val RC_SIGN_IN = 1
    private lateinit var user : FirebaseUser

    private lateinit var googleSignInClient: GoogleSignInClient

    //Vars Firebase
    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

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
        initFirebase()
        initGoogleAuth()
        clickBtn()
        clickRegister()
        changeTextBtnGoogle()
    }


    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initFirebase() {
        FireStore = FirebaseFirestore.getInstance()
        Auth = Firebase.auth
    }

    private fun changeTextBtnGoogle(){
        val googleButton = findViewById<View>(R.id.google_button) as SignInButton
        for (i in 0 until googleButton.childCount) {
            val v = googleButton.getChildAt(i)
            if (v is TextView) {
                v.textSize = 18f
                v.setTypeface(null, Typeface.NORMAL)
                v.text = getString(R.string.login_google)
                v.setTextColor(R.color.colorBackground)
                v.isSingleLine = true
                v.setPadding(150, 20, 50, 20)
                return
            }
        }
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

        Auth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("login", task.exception.toString())
                    initNavigation()
                }
                else{
                    if (task.exception is FirebaseAuthUserCollisionException)
                        txtUser_Login.error = getString(R.string.errorLoginEmail)
                    else if (task.exception is FirebaseAuthInvalidCredentialsException)
                        txtPwd_Login.error = getString(R.string.errorLoginPWD)
                    else
                        applicationContext.toast(R.string.errorService)
                }
            }
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

        google_button.setOnClickListener {
            signInGoogle()
        }

        twitter_button.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {

                val session = TwitterCore.getInstance().sessionManager.activeSession
                val authClient = TwitterAuthClient()
                authClient.requestEmail(session, object: Callback<String>(){
                    override fun success(result: Result<String>?) {
                        //Log.i("emailTwitter", result!!.data[0].toString())
                    }

                    override fun failure(exception: TwitterException?) {
                    }

                })
                handleTwitterLogin(session)

            }

            override fun failure(exception: TwitterException?) {

            }

        }

    }

    private fun handleTwitterLogin(session: TwitterSession) {
        val credential = TwitterAuthProvider.getCredential(
            session.authToken.token,
            session.authToken.secret
        )

        Auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("", "signInWithCredential:success")
                val user = Auth.currentUser
                insertarUser(user!!)
                initNavigation()
            } else {
                // If sign in fails, display a message to the user.
                applicationContext.toast(R.string.errorService)
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
        finish()
    }

    /**
     * Metodo que contiene el intent que permite ir a la actividad NavigataionDrawer
     */
    private fun initNavigation() {
        (application as MyApplication).USUARIO_FIRE = user
        val intent = Intent(this, NavigationDrawer::class.java)
        startActivity(intent)
        finish()

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("", "signInWithCredential:success")
                    user = Auth.currentUser!!
                    insertarUser(user!!)
                    initNavigation()
                } else {
                    // If sign in fails, display a message to the user.
                    applicationContext.toast(R.string.errorService)
                }
            }
    }

    private fun insertarUser(user: FirebaseUser) {
        val userFB = UserFB(
            user.uid,
            user.displayName,
            user.displayName,
            user.email,
            "",
            "",
            "",
            user.photoUrl.toString()
        )
        FireStore.collection("users")
            .document(user.uid)
            .set(userFB)
            .addOnSuccessListener {
                Log.i("insertarUserGoogle", "ok")
            }
            .addOnFailureListener {
                applicationContext.toast(R.string.errorService)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        twitter_button.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.i("gs", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.i("gs", "Google sign in failed", e)
                // ...
            }
        }
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