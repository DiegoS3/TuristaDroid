package android.com.diego.turistadroid.navigation_drawer.ui.newplace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.utilities.slider.SliderAdapter
import android.com.diego.turistadroid.utilities.slider.SliderItem
import android.os.Handler
import android.util.Log
import android.widget.RatingBar
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlin.math.abs


class NewPlaceFragment : Fragment(), RatingBar.OnRatingBarChangeListener {

    private lateinit var viewPager2 : ViewPager2
    private lateinit var ratingBar : RatingBar
    private lateinit var adapter: SliderAdapter
    private var sliderHandler = Handler()
    //Lista de imagenes
    private var sliderItems =  mutableListOf<SliderItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_newplace, container, false)
        viewPager2 = root.findViewById(R.id.vpImagesPlace_NewPlace)
        ratingBar = root.findViewById(R.id.ratingBarPlace_NewPlace)
        ratingBar.onRatingBarChangeListener = this

        // Inflate the layout for this fragment
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLista()
        adapter = SliderAdapter(sliderItems, viewPager2)
        viewPager2.adapter = adapter
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->

            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f

        }

        viewPager2.setPageTransformer(compositePageTransformer)

        //Metodo para que las iamgenes se pasen solas
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }

        })


    }

    private fun initLista(){

        val image = SliderItem(R.drawable.ima_default_place)
        sliderItems.add(image)
        sliderItems.add(image)
        sliderItems.add(image)
        sliderItems.add(image)

    }

    private var sliderRunnable = Runnable {

        run {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }

    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    //Obtener puntuacion del sitio
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
       Log.i("Star", rating.toString())
    }

}