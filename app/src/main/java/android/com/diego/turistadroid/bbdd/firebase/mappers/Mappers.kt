package android.com.diego.turistadroid.bbdd.firebase.mappers

import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB

object Mappers {

    /**
     * Devuelve un usuario a partir de un documento
     * del Cloud Firestore de Firebase.
     *
     * @param doc: Map<String, Any>
     * @return UserFB
     */
    fun dtoToUser(doc: Map<String, Any>) = UserFB(
        id = doc["id"].toString(),
        name = doc["name"].toString(),
        userName = doc["userName"].toString(),
        email = doc["email"].toString(),
        pwd = doc["pwd"].toString(),
        insta = doc["insta"].toString(),
        twitter = doc["twitter"].toString(),
        foto = doc["foto"].toString()
    )
}