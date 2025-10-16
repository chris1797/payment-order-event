package com.msa.payment.api.user

import com.msa.payment.domain.user.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
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

    @PatchMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.signIn(request))
    }
}