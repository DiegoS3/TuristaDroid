package android.com.diego.turistadroid.utilities.slider

import android.com.diego.turistadroid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.slide_item_container.view.*

class SliderAdapter (

    private var sliderItems : MutableList<SliderItem>,
    private var viewPager2 : ViewPager2

) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {

        return SliderViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {

        holder.setImage(sliderItems[position])

        if (position == sliderItems.size - 2){
            viewPager2.post(runnable)
        }

    }

    override fun getItemCount(): Int {
       return sliderItems.size
    }

    private var runnable = Runnable {

        kotlin.run {
            sliderItems.addAll(sliderItems)
            notifyDataSetChanged()
        }

    }

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        var imageView: RoundedImageView = itemView.imgSlide

        fun setImage(sliderItem: SliderItem){

            imageView.setImageBitmap(sliderItem.getImage())
        }

    }

}