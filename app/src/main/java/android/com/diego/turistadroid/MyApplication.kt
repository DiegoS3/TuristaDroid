package android.com.diego.turistadroid

import android.app.Application
import android.com.diego.turistadroid.bbdd.ControllerBbdd


class MyApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        ControllerBbdd.initRealm(this)
    }
}