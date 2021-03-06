package android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit

import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions.SessionsDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.users.UserDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesDTO
import retrofit2.Call
import retrofit2.http.*

interface BBDDRest {

    //--USERS--


    // Obtener todos los usuarios
    @GET("users/")
    fun selectAllUsers(): Call<List<UserDTO>>

    // Obtener un usuario por id
    @GET("users/{id}")
    fun selectUserById(@Path("id") id: String): Call<UserDTO>
    // Obtener un usuario por id


    // Obtener un usuario por email
    @GET("users/")
    fun selectUserByEmail(@Query("email") email: String): Call<List<UserDTO>>

    // Obtener un usuario por userName
    @GET("users/")
    fun selectUserByUserName(@Query("userName") userName: String): Call<List<UserDTO>>

    // Insertar un usuario
    @POST("users/")
    fun insertUser(@Body user: UserDTO): Call<UserDTO>

    // Elimina un usuario
    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: String): Call<UserDTO>

    // Actualizar un usuario
    @PUT("users/{id}")
    fun updateUser(@Path("id") id: String, @Body user: UserDTO): Call<UserDTO>


    //--PLACES--


    // Obtener todos los lugares
    @GET("places/")
    fun selectAllPlaces(): Call<List<PlacesDTO>>

    // Elimina un lugar
    @GET("places/{id}")
    fun selectPlaceById(@Path("id") id: String): Call<PlacesDTO>

    // Obtener los lugares segun el id del usuario
    @GET("places/")
    fun selectPlaceByIdUser(@Query("idUser") idUser: String): Call<List<PlacesDTO>>

    // Obtener los lugares cerca de la posicion actual
    //To do
    @GET("places/{id}")
    fun selectNearbyPlaces(@Path("id") id: String): Call<List<PlacesDTO>>

    // Insertar un lugar
    @POST("places/")
    fun insertPlace(@Body place: PlacesDTO): Call<PlacesDTO>

    // Elimina un lugar
    @DELETE("places/{id}")
    fun deletePlace(@Path("id") id: String): Call<PlacesDTO>

    // Actualizar un lugar
    @PUT("places/")
    fun updatePlace(@Query("id") id: String, @Body place: PlacesDTO): Call<PlacesDTO>

    //Actualizar votos de un lugar
    @FormUrlEncoded
    @PATCH("places/{id}")
    fun updateVotesPlace(@Path("id") id: String, @Field("votos") votos : ArrayList<String>): Call<PlacesDTO>


    //--IMAGES--


    // Obtener imagenes por id de lugar
    @GET("images/")
    fun selectImageByIdLugar(@Query("idLugar") idLugar: String): Call<List<ImagesDTO>>

    // Insertar una imagen
    @POST("images/")
    fun insertImage(@Body image: ImagesDTO): Call<ImagesDTO>

    // Eliminar imagenes por el id del lugar
    @DELETE("images/{id}")
    fun deleteImagesLugar(@Path("id") id: String): Call<ImagesDTO>


    //--SESSIONS--


    // Obtener una sesion por el id de un usuario
    @GET("sessions/{id}")
    fun selectSessionById(@Path("id") id: String): Call<SessionsDTO>

    // Insertar una sesion
    @POST("sessions/")
    fun insertSession(@Body session: SessionsDTO): Call<SessionsDTO>

    // Eliminar una session por el id de un usuario
    @DELETE("sessions/{id}")
    fun deleteSession(@Path("id") id: String): Call<SessionsDTO>

    //Actualiza fecha sesion
    @FormUrlEncoded
    @PATCH("sessions/{id}")
    fun updateDateSession(@Path("id") id: String, @Field("fecha") fecha : String): Call<SessionsDTO>

}