package android.com.diego.turistadroid.utilities

import android.com.diego.turistadroid.bbdd.Image
import android.com.diego.turistadroid.bbdd.Place
import android.com.diego.turistadroid.bbdd.User

class ImpExp(
    val users: MutableList<User>,
    val sites: MutableList<Place>,
    val images: MutableList<Image>
)