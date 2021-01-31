package android.com.diego.turistadroid.bbdd.realm.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Image (@PrimaryKey var id : Long = 0,
                  var foto : String = "") : RealmObject(), Serializable

