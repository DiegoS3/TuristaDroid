package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.bbdd.apibbdd.services.imgur.ImgurREST
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object UtilsApiImgur {

   lateinit var datos : JSONObject

    fun uploadImg(context: Context, foto: String){

        //var item = JSONObject()
        val client = OkHttpClient().newBuilder().build()
        val mediaType: MediaType = "text/plain".toMediaTypeOrNull()!!
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", foto)
            .build()
        val request = ImgurREST.postImage(body,"base64")
        Log.i("answer", request.toString()+" "+request.body.toString())
        client.newCall(request).enqueue(object : Callback{

            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(context,"Error uploading image",Toast.LENGTH_SHORT).show()
                Log.i("answer","fallo")
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful){

                    val data = JSONObject(response.body!!.string())
                    setDatos(data.getJSONObject("data"))
                    Log.i("answer","OK")
                }else
                {
                    Toast.makeText(context,"Error accessing service",Toast.LENGTH_SHORT).show()
                    Log.i("answer","NO")
                }
            }

        })
    }

    @JvmName("setDatos1")
    fun setDatos(jsonObject: JSONObject){
        this.datos = jsonObject
    }
}