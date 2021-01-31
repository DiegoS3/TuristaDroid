package android.com.diego.turistadroid.bbdd.apibbdd.entities.images

object ImagesMapper {

    /*
    * Convierte lista DTO al modelo serializable
    * @param items List<ImagesDTO>
    * @return List<Images>
    */
    fun fromDTO(items: List<ImagesDTO>): List<Images> {
        return items.map { fromDTO(it) }
    }

    /*
    * Convierte el serializable al DTO
    * @param items List<Images>
    * @return List<ImagesDTO>
    */
    fun toDTO(items: List<Images>): List<ImagesDTO> {
        return items.map { toDTO(it) }
    }

    /*
    * DTO a Modelo
    * @param dto ImagesDTO
    * @return Images
    */
    fun fromDTO(dto: ImagesDTO): Images {
        return Images(
            dto.id,
            dto.idLugar,
            dto.url
        )
    }

    /*
    * Modelo a DTO
    * @param model Images
    * @return ImagesDTO
    */
    fun toDTO(model: Images): ImagesDTO {
        return ImagesDTO(
            model.id!!,
            model.idLugar!!,
            model.url!!
        )
    }
}