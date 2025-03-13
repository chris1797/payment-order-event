package core.carrotkotlin.domain

import core.carrotkotlin.dto.user.SignupRequest
import core.carrotkotlin.dto.user.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignupRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.signUp(request))
    }
}