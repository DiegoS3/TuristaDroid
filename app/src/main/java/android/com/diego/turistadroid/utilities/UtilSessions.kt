package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.Sessions
import android.content.Context
import android.util.Log
import com.google.gson.Gson

object UtilSessions {

    /**
    * Comrpueba si existe una sesión abierta
    * @param context Context
    * @return Boolean
    */
    private fun comprobarSesion(context: Context): Boolean {
        // Abrimos las preferencias en modo lectura
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val session = prefs.getString("SESSION", "").toString()
        Log.i("Config", "Usuario ID: $session")
        return session.isNotEmpty()
    }

    /**
     * Guarda la session y la fecha en el sharedPrefrence
     * @param context Context
     * @param session Sessions sesion actual del usuario
     * @param fecha String fecha actual
     */
    fun crearSesion(session: Sessions, fecha : String, context: Context) {
        // Abrimos las preferemcias en modo escritura
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("SESSION", Gson().toJson(session))
        editor.putString("FECHA", fecha)
        editor.apply()
    }

    /**
     * actualiza la fecha en el sharedPrefrence
     * @param context Context
     * @param fecha String fecha actual
     */
    fun actualizarFecha( fecha : String, context: Context) {
        // Abrimos las preferemcias en modo escritura
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("FECHA", fecha)
        editor.apply()
    }

    /**
     * Leemos la sesion activa el usuario
     * @param context Context
     * @return Usuario
     */
    private fun leerSesion(context: Context): Sessions {
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        return Gson().fromJson(prefs.getString("SESSION", ""), Sessions::class.java)
    }

    /**
     * Leemos la fecha activa del usuario
     * @param context Context
     * @return String
     */
    fun getFecha(context: Context): String {
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        return prefs.getString("FECHA", "")!!
    }

    fun eliminarSesion(context: Context){

        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("SESSION")
        editor.remove("FECHA")
        editor.apply()

    }

    /**
     * Lee los datos de una sesión local
     * @param context Context
     * @return Sessions?
     */
    fun getLocal(context: Context): Sessions? {
        if (comprobarSesion(context)) {
            return try {
                leerSesion(context)
            } catch (ex: Exception) {
                Log.i("Sesion", "Error al leer sesion: " + ex.localizedMessage)
                null
            }
        }
        return null
    }
}