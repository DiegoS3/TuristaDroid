package android.com.diego.turistadroid.bbdd.apibbdd.services.imgur

import android.util.Log
import okhttp3.*

object ImgurREST {

    //Obtenemos una imagen a partir de un ID
    fun getImage(hash: String): Request{

        return Request.Builder()
            .url(ImgurAPI.IMGUR_URL+hash)
            .method("GET", null)
            .addHeader("Authorization", "Client-ID "+ImgurAPI.CLIENT_ID)
            .build()
    }

    //Posteamos una imagen, pasando el body y el formato (En Base64 o URL)
    fun postImage(body: RequestBody, contType: String): Request{

        Log.i("answer","estoy en postImage")
        return Request.Builder()
            .url(ImgurAPI.IMGUR_URL)
            .post(body)
            .addHeader("Authorization", "Client-ID "+ImgurAPI.CLIENT_ID)
            .addHeader("Content-Type", contType)
            .build()
    }


}