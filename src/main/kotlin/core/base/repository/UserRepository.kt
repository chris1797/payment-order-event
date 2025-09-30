package core.base.repository

import core.base.domain.user.User
import core.base.domain.user.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUserName(userName: String): User?
    fun findByStatus(status: UserStatus): List<User>
    fun existsByUserName(userName: String): Boolean
}
