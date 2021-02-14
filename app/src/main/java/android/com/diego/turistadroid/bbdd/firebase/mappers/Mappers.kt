package android.com.diego.turistadroid.bbdd.firebase.mappers

import android.com.diego.turistadroid.bbdd.firebase.entities.ImageFB
import android.com.diego.turistadroid.bbdd.firebase.entities.PlaceFB
import android.com.diego.turistadroid.bbdd.firebase.entities.UserFB
import android.com.diego.turistadroid.bbdd.firebase.entities.VotoFB

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

    fun dtoToPlace(doc: Map<String, Any>) = PlaceFB(
        id = doc["id"].toString(),
        idUser = doc["idUser"].toString(),
        name = doc["name"].toString(),
        fecha = doc["fecha"].toString(),
        latitude = doc["latitude"].toString(),
        longitude = doc["longitude"].toString(),
        votos = doc["votos"].toString(),
        city = doc["city"].toString()
    )

    fun dtoToImage(doc: Map<String, Any>) = ImageFB(
        id = doc["id"].toString(),
        url = doc["url"].toString()
    )

    fun dtoToVoto(doc: Map<String, Any>) = VotoFB(
        idUser = doc["idUser"].toString()
    )
}