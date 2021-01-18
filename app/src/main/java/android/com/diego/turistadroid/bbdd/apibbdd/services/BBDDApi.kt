package android.com.diego.turistadroid.bbdd.apibbdd.services

import android.com.diego.turistadroid.utilities.Constants

object BBDDApi {

    //direccion de la api
    private const val API_URL = Constants.PROTOCOLO+Constants.SERVER_IP+":"+Constants.SERVER_PORT+"/"

    //constructor del servicio
    val service: BBDDRest
        get() = RetrofitClient.getClient(API_URL)!!.create(BBDDRest::class.java)
}