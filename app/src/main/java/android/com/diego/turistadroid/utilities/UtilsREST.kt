package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.Votes
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.com.diego.turistadroid.utilities.Utilities.toast
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UtilsREST {

    fun getVotos(id: String?, txtVotes : TextView?, context: Context) {

        val bbddRest = BBDDApi.service

        val call = bbddRest.selectVotesById(id!!)

        call.enqueue(object : Callback<VotesDTO>{
            override fun onResponse(call: Call<VotesDTO>, response: Response<VotesDTO>) {

                if (response.isSuccessful){

                    val votesDTO = response.body()!!
                    val votes = VotesMapper.fromDTO(votesDTO)

                    val numVotos = votes.votesUsers!!.size

                    //actualizarLugar(id, numVotos, context)

                    txtVotes!!.text = numVotos.toString()

                }else{
                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<VotesDTO>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarLugar(id: String?, numVotos : Int, context: Context){
        val bbddRest = BBDDApi.service
        val call = bbddRest.updateVotesPlace(id!!, numVotos.toString())

        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {
                if (response.isSuccessful){
                    Log.i("lugar", "votos lugar actualizados")
                }else{
                    Log.i("lugar", "error actualizar votos lugar")
                }
            }
            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })

    }

    /*private fun actualizarVoto(vote: Votes, idPlace: String){
        val bbddRest = BBDDApi.service
        val list = vote.votesUsers
        list!!.add((idUser))
        val call = bbddRest.updateVotes(idPlace, list)
        call.enqueue(object : Callback<VotesDTO>{
            override fun onResponse(call: Call<VotesDTO>, response: Response<VotesDTO>) {

            }
            override fun onFailure(call: Call<VotesDTO>, t: Throwable) {

            }
        })
    }*/
}