package core.carrotkotlin.domain.user

import core.carrotkotlin.api.user.LoginRequest
import core.carrotkotlin.api.user.SignupRequest
import core.carrotkotlin.api.user.UserResponse
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
        if (userRepository.findByEmail(request.email) != null) {
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

    fun signIn(request: LoginRequest): UserResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid password")
        }

        return UserResponse(
            id = user.id,
            email = user.email,
            name = user.name
        )
    }


}