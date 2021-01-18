package android.com.diego.turistadroid.bbdd.apibbdd.entities.images

import com.google.gson.annotations.SerializedName

class ImagesDTO(
    @SerializedName("id") val id : String,
    @SerializedName("idLugar") val idLugar : String,
    @SerializedName("url") val url: String
)