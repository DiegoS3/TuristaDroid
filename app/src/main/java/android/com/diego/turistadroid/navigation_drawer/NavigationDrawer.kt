package android.com.diego.turistadroid.navigation_drawer

import android.Manifest
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.navigation_drawer.ui.myplaces.MyPlacesFragment
import android.com.diego.turistadroid.utilities.Utilities
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.Menu
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_navigation_drawer.*

class NavigationDrawer : AppCompatActivity(){

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val user = LogInActivity.user
    private var CAMERA_PERMISSION = 2
    private var flashLightStatus: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /*val fab: FloatingActionButton = findViewById(R.id.btnFloatAddPlace_MyPlaces)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navHeader: View = navView.getHeaderView(0)
        val imaUser_nav = navHeader.findViewById<ImageView>(R.id.imgUser_nav)
        val txtNombreNav = navHeader.findViewById<TextView>(R.id.txtName_nav)
        val txtCorreoNav = navHeader.findViewById<TextView>(R.id.txtEmail_nav)

        //asigno los datos del usuario al navHeader.
        txtNombreNav.text = user.nombre
        txtCorreoNav.text = user.email
        imaUser_nav.setImageBitmap(Utilities.base64ToBitmap(user.foto))
        Utilities.redondearFoto(imaUser_nav)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_myPlaces, R.id.nav_myProfile, R.id.nav_nearMe, R.id.nav_lantern
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        linternaListener(navView)
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

    private fun abrirMyPlaces(){

        val newFragment = MyPlacesFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun linternaListener(navigationView: NavigationView){
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_lantern -> {
                    linterna()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true

                }
                R.id.nav_myPlaces -> {
                    abrirMyPlaces()
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


    private fun setupPermissions(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_PERMISSION -> {
                if (grantResults.isEmpty() || !grantResults[0].equals((PackageManager.PERMISSION_GRANTED))) {
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                } else {
                    openFlashLight()
                }
            }
        }
    }

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
            Toast.makeText(this, "This device has no flash", Toast.LENGTH_SHORT).show();
        }




    }
}

