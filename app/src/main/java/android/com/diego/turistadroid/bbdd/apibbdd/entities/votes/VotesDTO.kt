package android.com.diego.turistadroid.bbdd.apibbdd.entities.votes

import com.google.gson.annotations.SerializedName
import java.util.*

class VotesDTO(
    @SerializedName("idPlace") val idPlace : String,
    @SerializedName("votesUsers") val votesUsers : LinkedList<String>
)