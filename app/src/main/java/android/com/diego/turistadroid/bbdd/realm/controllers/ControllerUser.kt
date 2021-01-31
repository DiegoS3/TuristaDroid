package android.com.diego.turistadroid.bbdd.realm.controllers

import android.com.diego.turistadroid.bbdd.realm.entities.User
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where


object ControllerUser {

    //QUERIES TABLA USER

    /*ARREGLAR EXPORTAR DATOS, -- 15 MIN
    IMPLEMENTAR TIEMPO, -- 30 MIN
    ARREGLAR VOTOS. -- 20 MIN
    DOCUMENTACION Y VIDEO. -- 2 HORAS
    CAMBIAR ICONOS APP Y MENUS
    */

    //Insertar Usuario
    fun insertUser(user: User){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(user)
        }
    }

    //Seleccionar usuario por email
    fun selectByEmail(email: String): User?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().equalTo("email", email).findFirst())
    }

    //Actualizar un usuario
    fun updateUser(user: User){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealmOrUpdate(user)
        }
    }

    //Comprobar si existe un nombre de Usuario
    fun uniqueUser(id: String?): Boolean {
        val query: RealmResults<User> = Realm.getDefaultInstance().where<User>().equalTo("nombreUser", id).findAll()
        return query.size != 0
    }

    //Eliminar un Usuario
    fun deleteUser(email: String){
        Realm.getDefaultInstance().executeTransaction{
            it.where<User>().equalTo("email", email).findFirst()?.deleteFromRealm()
        }
    }

    //Eliminar todos los usuarios
    fun deleteAllUsers(){
        Realm.getDefaultInstance().executeTransaction{
            it.where<User>().findAll().deleteAllFromRealm()
        }
    }

    //Seleccionar todos los usaers
    fun selectUsers(): MutableList<User>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().findAll()
        )
    }
}