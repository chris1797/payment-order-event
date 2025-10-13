package core.base.domain.user

object UserFixture {

    fun createUser(
        name: String = "John Doe",
        address: String = "123 Main St",
        status: UserStatus = UserStatus.ACTIVE,
    ): User {
        return User(
            userName = name,
            address = address,
            status = status
        )
    }
}