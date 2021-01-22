package android.com.diego.turistadroid.bbdd.apibbdd.entities.users

object UserMapper {

    /*
    * Convierte lista DTO al modelo serializable
    * @param items List<UsuarioDTO>
    * @return List<Usuario>
    */
    fun fromDTO(items: List<UserDTO>): List<UserApi> {
        return items.map { fromDTO(it) }
    }

    /*
    * Convierte el serializable al DTO
    * @param items List<Usuario>
    * @return List<UsuarioDTO>
    */
    fun toDTO(items: List<UserApi>): List<UserDTO> {
        return items.map { toDTO(it) }
    }

    /*
    * DTO a Modelo
    * @param dto UsuarioDTO
    * @return Usuario
    */
    fun fromDTO(dto: UserDTO): UserApi {
        return UserApi(
            dto.id,
            dto.name,
            dto.userName,
            dto.email,
            dto.pwd,
            dto.insta,
            dto.twitter,
            dto.foto
        )
    }

    /*
    * Modelo a DTO
    * @param model Usuario
    * @return UsuarioDTO
    */
    fun toDTO(model: UserApi): UserDTO {
        return UserDTO(
            model.id!!,
            model.name!!,
            model.userName!!,
            model.email!!,
            model.pwd!!,
            model.insta!!,
            model.twitter!!,
            model.foto!!
        )
    }

}