package core.payment.domain.user

import core.payment.api.user.LoginRequest
import core.payment.api.user.UserResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `정상 로그인 시 UserResponse 반환`() {
        // Given
        val email = "chris@abc.com"
        val password = "password"
        val request = LoginRequest(email, password)
        val user = User(
            id = 1L,
            name = "Chris",
            email = email,
            password = password
        )

        `when`(userRepository.findByEmail(email)).thenReturn(user)
        `when`(passwordEncoder.matches(request.password, user.password)).thenReturn(true)

        // When
        val response: UserResponse = userService.signIn(request)

        // Then
        assertThat(response.email).isEqualTo(email)
        verify(passwordEncoder).matches(request.password, user.password)
    }

}