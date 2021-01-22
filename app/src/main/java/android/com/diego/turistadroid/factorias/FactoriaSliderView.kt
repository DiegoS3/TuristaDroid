package android.com.diego.turistadroid.factorias

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.utilities.slider.SliderAdapterExample
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView

class FactoriaSliderView {

    companion object{

        var adapterSlider: SliderAdapterExample? = null

        fun initSliderView(view : View, context : Context){
            val sliderView = view.findViewById<SliderView>(R.id.imageSlider)
            adapterSlider = SliderAdapterExample(context)
            sliderView.setSliderAdapter(adapterSlider!!)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM) //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
            sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            sliderView.indicatorSelectedColor = Color.WHITE
            sliderView.indicatorUnselectedColor = Color.GRAY
            sliderView.scrollTimeInSec = 3
            sliderView.isAutoCycle = true
            sliderView.startAutoCycle()
            sliderView.setOnIndicatorClickListener {
                Log.i(
                    "GGG",
                    "onIndicatorClicked: " + sliderView.currentPagePosition
                )
            }
        }
    }
}