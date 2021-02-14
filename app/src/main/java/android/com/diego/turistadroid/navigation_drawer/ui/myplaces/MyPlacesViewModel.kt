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
import android.com.diego.turistadroid.bbdd.firebase.entities.PlaceFB
import android.com.diego.turistadroid.bbdd.firebase.entities.VotoFB
import android.com.diego.turistadroid.bbdd.firebase.mappers.Mappers
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.item_list_places.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class MyPlacesViewModel(

    private val listPlaces: MutableList<PlaceFB>,
    private val listener: (PlaceFB) -> Unit

) : RecyclerView.Adapter<MyPlacesViewModel.PlaceViewHolder>() {

    private val context = MyPlacesFragment.myContext
    private val idUser = MyPlacesFragment.idUser
    private var votado = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {

        return PlaceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_places, parent, false)
        )
    }


    //Cargamos las imagenes
    private fun cargarImagen(holder: PlaceViewHolder, item: PlaceFB) {
        val FireStore = FirebaseFirestore.getInstance()
        FireStore.collection("places")
            .document(item.id!!)
            .collection("images")
            .get()
            .addOnSuccessListener { document ->
                if (!document.isEmpty) {
                    val image = document.documents[0].data?.get("url")
                    Glide.with(context)
                        .load(image)
                        .fitCenter()
                        .into(holder.imgItemPlace)
                }

            }
            .addOnFailureListener { e ->
                Log.i("cargarImage", e.localizedMessage!!)
            }
        /*
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            if(task.result.documents.size>0){
                val image = task.result.documents[0].data?.get("url")
                Glide.with(context)
                    .load(image)
                    .fitCenter()
                    .into(holder.imgItemPlace)
            }

        }
    }

         */
    }

    /**
     * Elimina un item de la lista
     *
     * @param pos
     */
    fun deleteItem(pos: Int) {

        listPlaces.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, listPlaces.size)

    }

    /**
     * Añade un item de la lista
     *
     * @param pos
     */
    fun addItem(place: PlaceFB) {
        listPlaces.add(place)
        notifyDataSetChanged()
    }

    /**
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun updateItem(item: PlaceFB, position: Int) {
        listPlaces[position] = item
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listPlaces.size)
        Log.i("lugared", item.toString())
    }


    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val item = listPlaces[position]
        val fecha = item.fecha!!.format("dd/M/yyyy")
        cargarImagen(holder, item)

        holder.txtTitleItemPlace.text = item.name
        holder.txtCityItemPlace.text = item.city
        holder.txtDateItemPlace.text = fecha
        holder.txtMarkItemPlace.text = listPlaces[position].votos
        //checkNumVotos(listPlaces[position], holder)

        yaVotado(listPlaces[position], holder, false)

        holder.itemView
            .setOnClickListener {
                listener(listPlaces[position])
            }
        holder.btnFavPlace.setOnClickListener {
            doFavPlace(position, holder)
        }
    }

    private fun doFavPlace(position: Int, holder: PlaceViewHolder) {
        yaVotado(listPlaces[position], holder, true)
        actualizarPlace(listPlaces[position], holder)
        //checkNumVotos(listPlaces[position], holder)
    }

    private fun yaVotado(place: PlaceFB, holder: PlaceViewHolder, click: Boolean) {
        val Firestore = FirebaseFirestore.getInstance()
        Firestore.collection("places")
            .document(place.id!!)
            .collection("votos")
            .get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.i("votos", "vacio")
                    holder.btnFavPlace.drawable.setTint(R.color.colorPrimary)//NO AMARILLO
                    if (click) {
                        Log.i("votos", "vacio y click")
                        setVotado(false)
                        holder.btnFavPlace.drawable.setTintList(null)//AMARILLO
                    }

                } else {
                    Log.i("votos", "voto/votos")
                    holder.btnFavPlace.drawable.setTint(R.color.colorPrimary)//NO AMARILLO
                    //setVotado(false)
                    var existe = false
                    var i = 0
                    do {
                        val voto = Mappers.dtoToVoto(documents.documents[i].data!!)
                        if (voto.idUser == idUser) {
                            existe = true
                            Log.i("votos", "existe voto de usuario")
                            holder.btnFavPlace.drawable.setTintList(null)//AMARILLO
                            if (click) {
                                Log.i("votos", "existe voto de usuario y click")
                                setVotado(true)
                            } else {
                                Log.i("votos", "existe voto de usuario y no click")
                            }
                        }
                        i++
                    } while (!existe && i < documents.size())

                    if (!existe)
                        setVotado(false)
                        Log.i("votos", "no existe este usuario")
                }
            }

    }

    private fun setVotado(votado: Boolean) {
        this.votado = votado
    }

    private fun actualizarPlace(place: PlaceFB, holder: PlaceViewHolder) {
        var votos = place.votos!!.toInt()
        if (votado) {
            eliminarVoto(place)
            holder.btnFavPlace.drawable.setTint(R.color.colorPrimary)
            votos--
        } else {
            crearVoto(place)
            holder.btnFavPlace.drawable.setTintList(null)
            votos++
        }
        updatePlaceFirebase(votos, place)
    }

    private fun updatePlaceFirebase(votos: Int, place: PlaceFB) {
        val Firestore = FirebaseFirestore.getInstance()
        Firestore.collection("places")
            .document(place.id!!)
            .update("votos", votos)
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }


    private fun crearVoto(place: PlaceFB) {
        val Firestore = FirebaseFirestore.getInstance()
        val voto = VotoFB(idUser)
        Firestore.collection("places")
            .document(place.id!!)
            .collection("votos")
            .document(idUser)
            .set(voto)
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    private fun eliminarVoto(place: PlaceFB) {
        val Firestore = FirebaseFirestore.getInstance()
        Firestore.collection("places")
            .document(place.id!!)
            .collection("votos")
            .document(idUser)
            .delete()
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    private fun checkSizeVotos(place: Places) {
        if (place.votos!!.size == 1) {
            place.votos.add("a")
        }
    }

    private fun checkNumVotos(place: Places, holder: PlaceViewHolder) {
        if (place.votos!!.size < 2) {
            holder.txtMarkItemPlace.text = "0"
        } else if (place.votos.size >= 2) {
            holder.txtMarkItemPlace.text = (place.votos.size - 1).toString()
        }
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