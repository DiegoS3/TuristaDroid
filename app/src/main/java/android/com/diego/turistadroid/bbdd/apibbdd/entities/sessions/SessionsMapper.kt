package android.com.diego.turistadroid.bbdd.apibbdd.entities.sessions

object SessionsMapper {

    /*
    * Convierte lista DTO al modelo serializable
    * @param items List<SessionsDTO>
    * @return List<Sessions>
    */
    fun fromDTO(items: List<SessionsDTO>): List<Sessions> {
        return items.map { fromDTO(it) }
    }

    /*
    * Convierte el serializable al DTO
    * @param items List<Sessions>
    * @return List<SessionsDTO>
    */
    fun toDTO(items: List<Sessions>): List<SessionsDTO> {
        return items.map { toDTO(it) }
    }

    /*
    * DTO a Modelo
    * @param dto SessionsDTO
    * @return Sessions
    */
    fun fromDTO(dto: SessionsDTO): Sessions {
        return Sessions(
            dto.idUser,
            dto.fecha
        )
    }

    /*
    * Modelo a DTO
    * @param model Sessions
    * @return SessionsDTO
    */
    fun toDTO(model: Sessions): SessionsDTO {
        return SessionsDTO(
            model.idUser!!,
            model.fecha!!
        )
    }

}