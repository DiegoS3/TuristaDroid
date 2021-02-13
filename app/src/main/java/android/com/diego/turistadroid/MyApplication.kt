package android.com.diego.turistadroid

import android.Manifest
import android.app.Application
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserApi
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig


class MyApplication : Application(){

    // Propiedades, getters and setters (visibilidad)
    var APP_PERMISOS = false
        private set

    lateinit var USUARIO_API: UserApi
    lateinit var USUARIO_FIRE: FirebaseUser

    override fun onCreate() {
        super.onCreate()
        initTwitterAuth()
    }


    /**
     * Comprobamos los permisos de la aplicación
     */
    fun initPermisos() {
        Log.i("Config", "Init Permisos")
        // Indicamos el permisos y el manejador de eventos de los mismos
        Dexter.withContext(this)
            // Lista de permisos a comprobar
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
            )
            // Listener a ejecutar
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // ccomprbamos si tenemos los permisos de todos ellos
                    if (report.areAllPermissionsGranted()) {
                        APP_PERMISOS = true

                    }

                    // comprobamos si hay un permiso que no tenemos concedido ya sea temporal o permanentemente
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // abrimos un diálogo a los permisos
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {  }
            .onSameThread()
            .check()
        Log.i("Config", "Fin Permisos")
    }

     private fun initTwitterAuth() {
        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret)))
            .debug(true)
            .build()
        Twitter.initialize(config)
    }
}