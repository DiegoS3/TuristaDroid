package android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions

import com.google.gson.annotations.SerializedName

class SessionsDTO(
    @SerializedName("idUser") val idUser : String,
    @SerializedName("fecha") val fecha : String
)