package android.com.diego.turistadroid.bbdd.realm.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Session (@PrimaryKey var emailUser : String = "") : RealmObject()