package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.images.ImagesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.Places
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.entities.places.PlacesMapper
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.Votes
import android.com.diego.turistadroid.bbdd.apibbdd.entities.votes.VotesDTO
import android.com.diego.turistadroid.bbdd.apibbdd.services.retrofit.BBDDApi
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.item_list_places.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class MyPlacesViewModel (

    private val listPlaces: MutableList<Places>,
    private val listener: (Places) -> Unit

) : RecyclerView.Adapter<MyPlacesViewModel.PlaceViewHolder>(){

    private val context = MyPlacesFragment.myContext
    private val idUser = MyPlacesFragment.idUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {

        return PlaceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_places, parent, false)
        )
    }

    //Cargamos las imagenes
    private fun cargarImagen(holder: PlaceViewHolder, item : Places ){
        val bbddRest = BBDDApi.service
        val call = bbddRest.selectImageByIdLugar(item.id!!)

        call.enqueue(object : Callback<List<ImagesDTO>>{
            override fun onResponse(call: Call<List<ImagesDTO>>, response: Response<List<ImagesDTO>>) {
                // Si la respuesta es correcta
                if (response.isSuccessful) {
                    if(response.body()!!.isNotEmpty()){

                        val imagesDTO = response.body()!!
                        val images = ImagesMapper.fromDTO(imagesDTO)

                        Glide.with(context)
                            .load(images[0].url)
                            .fitCenter()
                            .into(holder.imgItemPlace)
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.errorLogin), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ImagesDTO>>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.errorService), Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Elimina un item de la lista
     *
     * @param pos
     */
    fun deleteItem(pos : Int){

        listPlaces.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, listPlaces.size)

    }

    /**
     * Añade un item de la lista
     *
     * @param pos
     */
    fun addItem(place: Places){
        listPlaces.add(place)
        notifyDataSetChanged()
    }

    /**
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun updateItem(item: Places, position: Int) {
        listPlaces[position] = item
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listPlaces.size)
    }

    /**
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun restoreItem(item: Places, position: Int) {
        listPlaces.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listPlaces.size)
    }


    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val item = listPlaces[position]
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val fecha = item.fecha!!.format("dd/M/yyyy")
        cargarImagen(holder, item)

        holder.txtTitleItemPlace.text = item.name
        holder.txtCityItemPlace.text = item.city
        holder.txtDateItemPlace.text = fecha
        holder.txtMarkItemPlace.text = item.votos!!.size.toString()

        if (yaVotado(listPlaces[position])){
            holder.btnFavPlace.drawable.setTint(R.color.colorPrimary)
        }else{
            holder.btnFavPlace.drawable.setTintList(null)
        }

        holder.itemView
            .setOnClickListener {
                listener(listPlaces[position])
            }
        holder.btnFavPlace.setOnClickListener {
            doFavPlace(position, holder)
        }
    }

    private fun doFavPlace(position: Int, holder: PlaceViewHolder) {
        Log.i("votos","votado")
        actualizarPlace(listPlaces[position], holder)
        val votos : Int = listPlaces[position].votos!!.size
        val id = listPlaces[position].id!!
        holder.txtMarkItemPlace.text = votos.toString()
    }

    private fun yaVotado(places: Places): Boolean {
        var votado = false
        for (id in places.votos!!){
            if (id == idUser){
                votado = true
            }
        }
        return votado
    }

    private fun actualizarPlace(place: Places, holder: PlaceViewHolder) {

        val bbddRest = BBDDApi.service
        var listaVotos: MutableList<String>
        if (yaVotado(place)){
            place.votos!!.remove(idUser)
            listaVotos = place.votos
            holder.btnFavPlace.drawable.setTint(R.color.colorPrimary)
        }else{
            place.votos!!.add(idUser)
            listaVotos = place.votos
            holder.btnFavPlace.drawable.setTintList(null)
        }
        //val newPlace = Places(place.id, place.idUser, place.name, place.fecha, place.latitude, place.longitude, place.votos, place.city)
        //val call = bbddRest.updatePlace(place.id!!, PlacesMapper.toDTO(newPlace))
        val call = bbddRest.updateVotesPlace(place.id!!, listaVotos)
        call.enqueue(object : Callback<PlacesDTO>{
            override fun onResponse(call: Call<PlacesDTO>, response: Response<PlacesDTO>) {

            }
            override fun onFailure(call: Call<PlacesDTO>, t: Throwable) {

            }
        })
    }

    override fun getItemCount(): Int {
        return listPlaces.size
    }
    /**
     * Holder que encapsula los objetos a mostrar en la lista
     */
    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Elementos graficos con los que nos asociamos
        var imgItemPlace = itemView.imgPlace_Place!!
        var txtTitleItemPlace = itemView.txtTitlePlace_Place!!
        var txtCityItemPlace = itemView.txtCityPlace_Place!!
        var txtDateItemPlace = itemView.txtDatePlace_Place!!
        var txtMarkItemPlace = itemView.txtMark_Place!!
        var btnFavPlace = itemView.imgStar_Place!!
    }

}