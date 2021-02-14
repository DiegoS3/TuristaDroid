package android.com.diego.turistadroid.bbdd.firebase.entities

class PlaceFB(
    var id : String?,
    val idUser: String?,
    val name: String?,
    val fecha: String?,
    val latitude: String?,
    val longitude: String?,
    val votos : String?,
    val city: String?
){
    override fun toString(): String {
        return "PlaceFB(id=$id, idUser=$idUser, name=$name, fecha=$fecha, latitude=$latitude, longitude=$longitude, votos=$votos, city=$city)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceFB

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (idUser?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (fecha?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (votos?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        return result
    }

}