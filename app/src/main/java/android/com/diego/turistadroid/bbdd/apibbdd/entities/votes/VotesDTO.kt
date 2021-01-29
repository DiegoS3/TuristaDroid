package android.com.diego.turistadroid.bbdd.apibbdd.entities.votes

import com.google.gson.annotations.SerializedName
import java.util.*

class VotesDTO(
    @SerializedName("id") val id : String,
    @SerializedName("votesUsers") val votesUsers : MutableList<String>
)