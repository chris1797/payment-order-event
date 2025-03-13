package core.carrotkotlin.domain

import core.carrotkotlin.dto.user.SignupRequest
import core.carrotkotlin.dto.user.UserResponse
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun signUp(request: SignupRequest): UserResponse {
        if (userRepository.findByEmail(request.email).isNotEmpty()) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = request.password,
            location = null
        )
        user.encodePassword(passwordEncoder)

        val savedUser = userRepository.save(user);
        return UserResponse(
            id = savedUser.id,
            email = savedUser.email,
            name = savedUser.name
        )
    }


}