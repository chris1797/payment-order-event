package core.base.domain.user


data class UserDto(
    val id: Long,
    val userName: String,
    val address: String,
    val status: UserStatus,
) {
    fun toEntity(): User? {
        return User(
            id = id,
            userName = userName,
            address = address,
            status = status
        )
    }

    companion object {
        fun from(user: User): UserDto {
            return UserDto(
                id = user.id!!,
                userName = user.userName,
                address = user.address,
                status = user.status,
            )
        }
    }
}

data class UserCreateDto(
    val userName: String,
    val address: String
) {
    fun toEntity(): User {
        return User(
            userName = userName,
            address = address
        )
    }
}

data class UserUpdateDto(
    val address: String?,
    val status: UserStatus?
)
