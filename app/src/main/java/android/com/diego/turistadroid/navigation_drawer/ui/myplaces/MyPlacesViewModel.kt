package android.com.diego.turistadroid.navigation_drawer.ui.myplaces

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.utilities.Utilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_places.view.*
import java.text.SimpleDateFormat

class MyPlacesViewModel (

    private val listPlaces: MutableList<Place>,
    private val listener: (Place) -> Unit

) : RecyclerView.Adapter<MyPlacesViewModel.PlaceViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {

        return PlaceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_places, parent, false)
        )
    }

    private fun cargarImagen(holder: PlaceViewHolder, item : Place ){
        if (item.imagenes.size > 0){
            val image = Utilities.base64ToBitmap(item.imagenes[0]!!.foto)
            holder.imgItemPlace.setImageBitmap(image)
        }
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
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun updateItem(item: Place, position: Int) {
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
    fun restoreItem(item: Place, position: Int) {
        listPlaces.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listPlaces.size)
    }


    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val item = listPlaces[position]
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val fecha = sdf.format(item.fecha)
        cargarImagen(holder, item)

        holder.txtTitleItemPlace.text = item.nombre
        holder.txtCityItemPlace.text = item.city
        holder.txtDateItemPlace.text = fecha
        holder.txtMarkItemPlace.text = item.puntuacion.toString()

        holder.itemView
            .setOnClickListener {
                listener(listPlaces[position])
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
    }

}