package core.carrotkotlin.domain

import core.carrotkotlin.enums.Role
import jakarta.persistence.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Entity
@Table(name = "users")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    val name: String? = null

    @Column(nullable = false, unique = true)
    val email: String? = null

    @Column(nullable = false)
    lateinit var password: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER


    fun encodePassword(encoder: BCryptPasswordEncoder) {
        this.password = encoder.encode(password)
    }
}