package android.com.diego.turistadroid.bbdd.apibbdd.services.imgur

import okhttp3.OkHttpClient

object HttpClient {

    private var okHttp: OkHttpClient? = null

    //Creamos un cliente OkHttp en caso de ser nulo
    fun getClient(): OkHttpClient? {

        if (okHttp == null){

            okHttp = OkHttpClient().newBuilder().build()

        }
        return okHttp
    }

}