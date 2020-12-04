
package android.com.diego.turistadroid.bbdd

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where

object ControllerBbdd {

    // Variables de
    private const val DATOS_BD = "TURISTA_BD_REALM"
    private const val DATOS_BD_VERSION = 3L

    //Iniciamos la base de datos
    fun initRealm(context: Context?) {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .name(DATOS_BD)
            .schemaVersion(DATOS_BD_VERSION) // Versión de esquema estamos trabajando, si lo cambiamos, debemos incrementar
            .deleteRealmIfMigrationNeeded() // Podemos borrar los datos que ya haya si cambiamos el esquema,
            .build()
        Realm.setDefaultConfiguration(config)
    }

    //QUERIES TABLA USER

    //Insertar Usuario
    fun insertUser(user: User){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(user)
        }
    }

    //Seleccionar todos los usuarios
    fun selectUsers(): MutableList<User>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().findAll()
        )
    }

    //Seleccionar usuario por email
    fun selectByEmail(email : String): User?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().equalTo("email", email).findFirst()

        )
    }

    //Seleccionar usuario por nameUser
    fun selectByNameUser(nameUser : String): User?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().equalTo("nombreUser", nameUser).findFirst()

        )
    }

    //Actualizar un usuario
    fun updateUser(user: User){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealmOrUpdate(user)
        }
    }

    //QUERIES TABLA IMAGE

    //Insertar una imagen
    fun insertImage(image: Image){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(image)
        }
    }

    //Eliminar una imagen
    fun deleteImage(id : Long){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Image>().equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    //QUERIES TABLA SESSION

    //Insertar una sesion
    fun insertSession(session: Session){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(session)
        }
    }

    //Eliminar una sesion
    fun deleteSession(email: String){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Image>().equalTo("email", email).findFirst()?.deleteFromRealm()
        }
    }

    //QUERIES GENERALES

    //Eliminar las tablas
    fun removeAll(){
        Realm.getDefaultInstance().executeTransaction{
            it.deleteAll()
        }
    }

    //Cerrar base de datos
    fun close(){
        Realm.getDefaultInstance().close()
    }
}

