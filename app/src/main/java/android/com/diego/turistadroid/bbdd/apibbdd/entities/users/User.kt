package android.com.diego.turistadroid.bbdd.apibbdd.entities.users

import java.io.Serializable

data class User(
    val id : String?,
    val name: String?,
    val userName: String?,
    val email: String?,
    val pwd: String?,
    val insta: String?,
    val twitter: String?,
    val foto: String?
):Serializable