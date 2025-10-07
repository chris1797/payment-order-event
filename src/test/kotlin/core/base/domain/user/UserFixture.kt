package core.base.domain.user

object UserFixture {

    fun createUser(
        name: String = "John Doe",
        status: UserStatus = UserStatus.ACTIVE,
    ): User {
        return User(
            userName = name,
            address = "123 Main St",
            status = status
        )
    }
}