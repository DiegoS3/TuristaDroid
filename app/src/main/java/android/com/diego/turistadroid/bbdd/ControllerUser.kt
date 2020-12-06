package android.com.diego.turistadroid.bbdd

import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where


object ControllerUser {

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
    fun selectByEmail(email: String): User?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().equalTo("email", email).findFirst()

        )
    }

    //Seleccionar usuario por nameUser
    fun selectByNameUser(nameUser: String): User?{
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

    //Comprobar que existe un nombre de Usuario
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
}