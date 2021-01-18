package android.com.diego.turistadroid.bbdd.apibbdd.entities.users

import com.google.gson.annotations.SerializedName

class UserDTO(
    @SerializedName("id") val id : String,
    @SerializedName("name") val name: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("email") val email: String,
    @SerializedName("pwd") val pwd: String,
    @SerializedName("insta") val insta: String,
    @SerializedName("twitter") val twitter: String,
    @SerializedName("foto") val foto: String
)