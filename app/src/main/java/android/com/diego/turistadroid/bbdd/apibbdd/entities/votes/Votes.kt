package android.com.diego.turistadroid.bbdd.apibbdd.entities.votes

import java.io.Serializable
import java.util.*

data class Votes(
    val idPlace : String?,
    val votesUsers: LinkedList<String>?
):Serializable