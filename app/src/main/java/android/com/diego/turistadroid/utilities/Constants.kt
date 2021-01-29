package android.com.diego.turistadroid.utilities

import java.time.format.DateTimeFormatter

object Constants {

    const val SERVER_IP = "192.168.1.100"
    const val SERVER_PORT = 9999
    const val PROTOCOLO = "http://"
    val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")!!
    const val MAX_TIME_SESSION = 2
    const val TIME_DELAYED : Long = 5000

}