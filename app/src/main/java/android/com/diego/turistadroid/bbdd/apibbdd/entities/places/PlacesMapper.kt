package android.com.diego.turistadroid.bbdd.apibbdd.entities.places

object PlacesMapper {

    /*
    * Convierte lista DTO al modelo serializable
    * @param items List<PlacesDTO>
    * @return List<Places>
    */
    fun fromDTO(items: List<PlacesDTO>): List<Places> {
        return items.map { fromDTO(it) }
    }

    /*
    * Convierte el serializable al DTO
    * @param items List<Places>
    * @return List<PlacesDTO>
    */
    fun toDTO(items: List<Places>): List<PlacesDTO> {
        return items.map { toDTO(it) }
    }

    /*
    * DTO a Modelo
    * @param dto PlacesDTO
    * @return Places
    */
    fun fromDTO(dto: PlacesDTO): Places {
        return Places(
            dto.id,
            dto.idUser,
            dto.name,
            dto.fecha,
            dto.latitude,
            dto.longitude,
            dto.votos,
            dto.city
        )
    }

    /*
    * Modelo a DTO
    * @param model Places
    * @return PlacesDTO
    */
    fun toDTO(model: Places): PlacesDTO {
        return PlacesDTO(
            model.id!!,
            model.idUser!!,
            model.name!!,
            model.fecha!!,
            model.latitude!!,
            model.longitude!!,
            model.votos!!,
            model.city!!
        )
    }

}