package android.com.diego.turistadroid.bbdd

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Image (@PrimaryKey var id : Long = 0,
                  var foto : String = "") : RealmObject(){

}

