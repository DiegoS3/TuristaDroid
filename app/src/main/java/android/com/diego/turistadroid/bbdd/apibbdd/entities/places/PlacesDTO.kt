package android.com.diego.turistadroid.bbdd.apibbdd.entities.places

import com.google.gson.annotations.SerializedName

class PlacesDTO(
    @SerializedName("id") val id : String,
    @SerializedName("idUser") val idUser : String,
    @SerializedName("name") val name: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("votos") val votos: MutableList<String>,
    @SerializedName("city") val city: String
)