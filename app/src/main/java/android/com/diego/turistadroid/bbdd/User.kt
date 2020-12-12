package android.com.diego.turistadroid.bbdd

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User (@PrimaryKey var email : String = "",
                 var nombre : String = "",
                 var nombreUser : String = "",
                 var pwd : String = "",
                 var foto : String = "",
                 var places : RealmList<Place> = RealmList(),
                 var twitter : String = "",
                 var instagram : String = "") : RealmObject()





