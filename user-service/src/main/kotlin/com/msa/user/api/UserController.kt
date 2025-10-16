package com.msa.user.api

import com.msa.user.api.dto.UserCreateRequest
import com.msa.user.api.dto.UserResponse
import com.msa.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping("/username/{userName}")
    fun getUserByUserName(@PathVariable userName: String): ResponseEntity<UserResponse> {
        val user = userService.getUserByUserName(userName)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users.map { UserResponse.from(it) })
    }

    @PostMapping
    fun createUser(@RequestBody request: UserCreateRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request.userName, request.address)
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user))
    }
}