package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.signup.SignUp
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.lang.Byte.decode
import java.security.MessageDigest
import kotlin.experimental.and


object Utilities {

    var valido = false

    var latiud = 0.0
    var longitud = 0.0

    fun redondearFoto(imagen: ImageView){
        val originalDrawable: Drawable = imagen.drawable
        var originalBitmap: Bitmap = (originalDrawable as BitmapDrawable).bitmap

        if (originalBitmap.width > originalBitmap.height){
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.height, originalBitmap.height);
        }else if (originalBitmap.width < originalBitmap.height) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.width);
        }
        val roundedDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
            Resources.getSystem(),
            originalBitmap
        )
        roundedDrawable.cornerRadius = originalBitmap.width.toFloat()
        imagen.setImageDrawable(roundedDrawable)
    }

    /**
     * Convierte un Bitmap a una cadena Base64
     *
     * @param bitmap Bitmap
     * @return Cadena Base64
     */
    fun bitmapToBase64(bitmap: Bitmap): String? {
        // Comrimimos al 60 % la imagen
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Convierte una cadena Base64 a Bitmap
     *
     * @param b64String cadena Base 64
     * @return Bitmap
     */
    fun base64ToBitmap(b64String: String): Bitmap? {
        val imageAsBytes: ByteArray = Base64.decode(b64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    fun hashString(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    fun validarEmail(txtEmail: EditText): Boolean{

        txtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.i("EMAIL WATCHER","pulsado")
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()) {
                    valido = true
                } else {
                    valido = false
                    txtEmail.error = "Invalid Email"
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        return valido


    }

    private fun updatePasswordStrengthView(password: String, progressBar: ProgressBar, strengthView: TextView, context: Context) {

        if (TextView.VISIBLE != strengthView.visibility)
            return

        if (TextUtils.isEmpty(password)) {
            strengthView.text = ""
            progressBar.progress = 0
            return
        }

        val str = PasswordStrength.calculateStrength(password)
        strengthView.text = str.getText(context)
        strengthView.setTextColor(str.color)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            progressBar.progressDrawable.colorFilter = BlendModeColorFilter(str.color, BlendMode.SRC_IN)
        } else {
            progressBar.progressDrawable.setColorFilter(str.color, PorterDuff.Mode.SRC_IN)
        }
        when {
            str.getText(context) == "Weak" -> {
                progressBar.progress = 25
            }
            str.getText(context) == "Medium" -> {
                progressBar.progress = 50
            }
            str.getText(context) == "Strong" -> {
                progressBar.progress = 75
            }
            else -> {
                progressBar.progress = 100
            }
        }
    }

    fun validarPassword(txtPass: EditText, progressBar: ProgressBar, strengthView: TextView, context: Context){
        txtPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrengthView(s.toString(), progressBar, strengthView, context)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    /**
     * Comprueba si está conectado a internet por algún medio
     * @param context Context?
     * @return Boolean
     */
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                Log.i("Internet", "ActiveNetworkInfo.isConnected")
                return true
            }
        }
        Log.i("Internet", "Sin red")
        return false
    }

    /**
     * Comprueba si esta el GPS Activo
     * @param context Context?
     * @return Boolean
     */
    fun isGPSAvaliable(context: Context?): Boolean {
        val locationManager = context?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return if (gpsStatus) {
            Log.i("GPS", "GPS Activado")
            true
        } else {
            Log.i("GPS", "GPS Desactivado")
            false
        }
    }




}