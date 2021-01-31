package android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions

import com.google.gson.annotations.SerializedName

class SessionsDTO(
    @SerializedName("id") val id : String,
    @SerializedName("idUser") val idUser : String,
    @SerializedName("fecha") val fecha : String
)