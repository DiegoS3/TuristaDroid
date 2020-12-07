package android.com.diego.turistadroid.utilities.slider

import android.graphics.Bitmap

class SliderItem (private var image: Bitmap) {

    //Here you can use String var to store url
    //If you want to load image from the internet

    fun getImage(): Bitmap {
        return image
    }

}