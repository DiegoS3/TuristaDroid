package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.R
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.ByteArrayOutputStream
import java.security.MessageDigest


object Utilities {

    /**
     * Genera un código de QR
     * @param text String
     * @return Bitmap
     */
    fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.rgb(0, 77, 64) else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("QR", "generateQRCode: ${e.message}")
        }
        return bitmap
    }

    //Vibracion movil
    fun vibratePhone(context: Context?) {
        val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    var valido = false

    var latiud = 0.0
    var longitud = 0.0

    //Redondear foto
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

    //Parse bitmap to uri
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    //Sha-256 pwd
    fun hashString(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    //validar email
    fun validarEmail(txtEmail: EditText, context: Context?): Boolean{

        txtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.i("EMAIL WATCHER","pulsado")
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(txtEmail.text.toString()).matches()) {
                    valido = true
                } else {
                    valido = false
                    txtEmail.error = context!!.getString(R.string.invalidEmail)
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        return valido
    }

    //Actualizar progressbar y textview segun pwd
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

    //Validar pwd
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

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
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
        return false
    }
}