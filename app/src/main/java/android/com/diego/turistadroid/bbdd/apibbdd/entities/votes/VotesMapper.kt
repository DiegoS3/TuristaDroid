package android.com.diego.turistadroid.bbdd.apibbdd.entities.votes

object VotesMapper {

    /*
    * Convierte lista DTO al modelo serializable
    * @param items List<VotesDTO>
    * @return List<Votes>
    */
    fun fromDTO(items: List<VotesDTO>): List<Votes> {
        return items.map { fromDTO(it) }
    }

    /*
    * Convierte el serializable al DTO
    * @param items List<Votes>
    * @return List<VotesDTO>
    */
    fun toDTO(items: List<Votes>): List<VotesDTO> {
        return items.map { toDTO(it) }
    }

    /*
    * DTO a Modelo
    * @param dto VotesDTO
    * @return Votes
    */
    fun fromDTO(dto: VotesDTO): Votes {
        return Votes(
            dto.id,
            dto.votesUsers
        )
    }

    /*
    * Modelo a DTO
    * @param model Votes
    * @return VotesDTO
    */
    fun toDTO(model: Votes): VotesDTO {
        return VotesDTO(
            model.id!!,
            model.votesUsers!!
        )
    }

}