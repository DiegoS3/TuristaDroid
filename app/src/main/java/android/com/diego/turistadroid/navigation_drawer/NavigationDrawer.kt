package android.com.diego.turistadroid.navigation_drawer

import android.Manifest
import android.com.diego.turistadroid.MyApplication
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerSession
import android.com.diego.turistadroid.bbdd.ControllerUser
import android.com.diego.turistadroid.bbdd.User
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.allplaces.AllPlaces
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
import android.com.diego.turistadroid.navigation_drawer.ui.myprofile.MyProfileFragment
import android.com.diego.turistadroid.navigation_drawer.ui.nearme.NearMeFragment
import android.com.diego.turistadroid.splash.SplashScreenActivity
import android.com.diego.turistadroid.utilities.UtilImpExp
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_navigation_drawer.*

class NavigationDrawer : AppCompatActivity(){

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var user: User
    private var CAMERA_PERMISSION = 2
    private var flashLightStatus: Boolean = false
    private lateinit var toolbar: Toolbar

    companion object{
        lateinit var imaUser_nav : ImageView
        lateinit var txtNombreNav : TextView
        lateinit var txtCorreoNav : TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer)
        toolbar = findViewById(R.id.toolbar)


        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.my_places)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navHeader: View = navView.getHeaderView(0)
        imaUser_nav = navHeader.findViewById(R.id.imgUser_nav)
        txtNombreNav = navHeader.findViewById(R.id.txtName_nav)
        txtCorreoNav = navHeader.findViewById(R.id.txtEmail_nav)

        userSwitch()
        asignarDatosUsuario()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_myPlaces, R.id.nav_myProfile, R.id.nav_nearMe, R.id.nav_lantern, R.id.nav_AllPlaces
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navigationListener(navView)
        initPermisos()
        comprobarConexion()

    }

    //usuario segunde donde entremos
    private fun userSwitch(){
        user = if(SplashScreenActivity.login) {
            LogInActivity.user
        }else{
            val listaSesion = ControllerSession.selectSessions()!!
            val emailSesion = listaSesion[0].emailUser
            ControllerUser.selectByEmail(emailSesion)!!
        }
    }

    /**
     * Inicia/ Comprueba los permisos de la App
     */
    private fun initPermisos() {
        if (!(this.application as MyApplication).APP_PERMISOS)
            (this.application as MyApplication).initPermisos()
    }

    /**
     * Comprueba que exista las conexiones para funcionar
     */
    private fun comprobarConexion() {
        // Comprobamos la red
        comprobarRed()
        comprobarGPS()
    }

    /**
     * Comprueba que haya red, si no llama a activarlo
     */
    private fun comprobarRed() {
        if (Utilities.isNetworkAvailable(applicationContext)) {

        } else {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a internet",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.colorAccent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    /**
     * Comprueba que existe GPS si no llama a activarlo
     */
    private fun comprobarGPS() {
        if (Utilities.isGPSAvaliable(applicationContext)) {

        } else {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a GPS",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.colorAccent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    fun asignarDatosUsuario(){
        //asigno los datos del usuario al navHeader.
        txtNombreNav.text = user.nombre
        txtCorreoNav.text = user.email

        if (user.foto != ""){
            imaUser_nav.setImageBitmap(Utilities.base64ToBitmap(user.foto))
            Utilities.redondearFoto(imaUser_nav)
        }else{
            imaUser_nav.setImageResource(R.drawable.ima_user)
        }
    }

    private fun linterna(){
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                setupPermissions()
            } else {
                openFlashLight()
            }
        } else {
            openFlashLight()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation_drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //Listener que detecta en el boton que se pulsa del menu del navigation drawer
    private fun navigationListener(navigationView: NavigationView){
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_lantern -> {//Lintera
                    linterna()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_myPlaces -> {//Mis lugares
                    abrirMyPlaces()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        toolbar.title = getString(R.string.my_places)
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_AllPlaces -> {//Todos los lugares
                    abrirAllPlaces()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        toolbar.title = getString(R.string.all_places)
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_myProfile -> {//Mi perfil
                    abrirMyProfile()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        toolbar.title = getString(R.string.my_profile)
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_nearMe -> {//Cerca de mi
                    abrirNearMe()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        toolbar.title = getString(R.string.near_me)
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_exit -> {//Salir
                    ejecutarExit()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_export -> {//exportar
                    UtilImpExp.export(this)
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_import ->{//importar
                    UtilImpExp.import(this)
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }

                else -> {
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    false
                }
            }
        }
    }

    //Abrir mis lugares
    private fun abrirMyPlaces(){
        val newFragment = MyPlacesFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Abrir todos los lugares
    private fun abrirAllPlaces(){
        val newFragment = AllPlaces()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Abir Mu pergil
    private fun abrirMyProfile(){
        val newFragment = MyProfileFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Abrir cerca de mi
    private fun abrirNearMe(){
        val newFragment = NearMeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    //Salir al login
    private fun ejecutarExit(){
        ControllerSession.deleteSession(user.email)
        val intent = Intent (this, LogInActivity::class.java)
        startActivity(intent)
    }


    private fun setupPermissions(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != (PackageManager.PERMISSION_GRANTED)) {

                } else {
                    openFlashLight()
                }
            }
        }
    }

    //Activar linterna
    private fun openFlashLight(){
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (!flashLightStatus){
                try {
                    cameraManager.setTorchMode(cameraId, true)
                    flashLightStatus = true
                }catch (e: CameraAccessException){

                }
            }else{
                try {
                    cameraManager.setTorchMode(cameraId, false)
                    flashLightStatus = false
                }catch (e: CameraAccessException){
                }
            }
        }else{
            Toast.makeText(this, "This device has no flash", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ControllerSession.deleteSession(user.email)
    }

}