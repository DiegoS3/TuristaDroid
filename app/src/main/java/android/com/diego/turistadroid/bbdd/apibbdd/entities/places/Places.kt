package android.com.diego.turistadroid.bbdd.apibbdd.entities.places

import java.io.Serializable

data class Places(
    val id : String?,
    val idUser: String?,
    val name: String?,
    val fecha: String?,
    val latitude: String?,
    val longitude: String?,
    val votos : MutableList<String>?,
    val city: String?
):Serializable