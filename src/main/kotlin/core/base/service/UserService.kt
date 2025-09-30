package core.base.service

import core.base.domain.user.User
import core.base.domain.user.UserDto
import core.base.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    fun getUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
                NoSuchElementException("User not found with id: $userId")
            }
    }


}
