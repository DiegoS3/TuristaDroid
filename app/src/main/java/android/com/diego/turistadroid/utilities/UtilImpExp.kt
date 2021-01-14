package android.com.diego.turistadroid.utilities

import android.app.AlertDialog
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.ControllerImages
import android.com.diego.turistadroid.bbdd.ControllerPlaces
import android.com.diego.turistadroid.bbdd.ControllerUser
import android.com.diego.turistadroid.login.LogInActivity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object UtilImpExp {
    /**
     * Cuadro de dialogo para la confirmacion de la exportacion de archivos
     */
    fun export(context: Context) {
        AlertDialog.Builder(context)
            .setIcon(R.drawable.ic_import)
            .setTitle(context.getText(R.string.caution))
            .setMessage(context.getText(R.string.messExport))
            .setPositiveButton(context.getString(R.string.confirmDelete)) { dialog, which -> exportFiles(context!!) }
            .setNegativeButton(context.getString(R.string.btnCancel), null)
            .show()
    }

    /**
     * Cuadro de dialogo para la confirmacion de la importacion de archivos
     */
    fun import(context: Context) {
        AlertDialog.Builder(context)
            .setIcon(R.drawable.ic_export)
            .setTitle(context.getText(R.string.caution))
            .setMessage(context.getText(R.string.messImport))
            .setPositiveButton(context.getString(R.string.confirmDelete)) { dialog, which -> importFiles(context) }
            .setNegativeButton(context.getString(R.string.btnCancel), null)
            .show()
    }

    /**
     * Exporta los datos de toda la BD menos SESSION
     * @param context Context
     * @return Boolean
     */
    fun exportFiles(context: Context) {
        //coge todos los datos de todos los modelos
        val users = ControllerUser.selectUsers()!!
        val sites = ControllerPlaces.selectPlaces()!!
        val images = ControllerImages.selectImages()!!
        val impExp = ImpExp(
            //users = users,
            sites = sites,
            //images = images
        )
        val impExpGson = Gson().toJson(impExp)
        // Archivo el objeto JSON
        fileExport(context, impExpGson.toString())
    }

    /**
     * Importa os datos
     * @param context Context
     * @return Boolean
     */
    fun importFiles(context: Context) {
        //mete el json en una variable y la exporta
        val input = fileImport(context)
        Log.i("import", input)
        val impExp = Gson().fromJson(input, ImpExp::class.java)
        if (impExp != null) {
            proccesImport(impExp,context)
            Log.i("import", impExp.toString())
        } else {
            Toast.makeText(context!!, context.getText(R.string.importok), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Archiva los datos
     * @param context Context
     * @param datos String
     * @return Boolean
     */
    fun fileExport(context: Context, datos: String){
        //guarda en la memoria interna del movil
        val dirImpExp =
            File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/TuristDroid")
        if (!dirImpExp.exists())
            dirImpExp.mkdir()
        val file = File(dirImpExp, "turistDroid.json")
        Log.i("import", file.toString())
        try {
            val strToBytes: ByteArray = datos.toByteArray()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Files.write(Paths.get(file.toURI()), strToBytes)
                Toast.makeText(context!!, context.getText(R.string.exportok), Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Log.i("impor", "Error: " + ex.localizedMessage)
        }
    }

    /**
     * Procesa el exportar los datos
     * @param impExp ImpExp
     * @param context Context
     * @return Boolean
     */
    private fun proccesImport(impExp: ImpExp, context: Context){
        // Vamos a insertar el usuario
        try {
            deleteAll()
            Log.i("import", "borrado todo")
            //impExp.users.forEach { ControllerUser.insertUser(it) }
            impExp.sites.forEach { ControllerPlaces.insertPlace(it)
                LogInActivity.user.places.add(it)
                Log.i("import", LogInActivity.user.places[0]!!.nombre)
                ControllerUser.updateUser(LogInActivity.user)}

            Log.i("import", "borrado todo")
            //impExp.images.forEach { ControllerImages.insertImage(it) }
            Toast.makeText(context!!,context.getText(R.string.importok), Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Log.i("import", "Error: " + ex.localizedMessage)
        }
    }

    /**
     * Borra todos los datos menos los de session
     */
    private fun deleteAll() {
        ControllerPlaces.deleteAllPlaces()
        //ControllerUser.deleteAllUsers()
        //ControllerImages.deleteAllImages()
    }
    /**
     * Importa los datos
     * @param context Context
     * @return String
     */
    fun fileImport(context: Context): String {
        //tiene que ser la misma ruta que el import
        val dirBackup =
            File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/TuristDroid")
        val file = File(dirBackup, "turistDroid.json")
        var datos: String = ""
        if (file.exists()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                datos = Files.readAllLines(Paths.get(file.toURI()))[0]
            }
        }
        return datos
    }

}