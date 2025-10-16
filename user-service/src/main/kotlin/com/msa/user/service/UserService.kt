package com.msa.user.service

import com.msa.user.domain.User
import com.msa.user.repository.UserRepository
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

    fun getUserByUserName(userName: String): User? {
        return userRepository.findByUserName(userName)
    }

    @Transactional
    fun createUser(userName: String, address: String): User {
        if (userRepository.existsByUserName(userName)) {
            throw IllegalArgumentException("User already exists with userName: $userName")
        }

        val user = User(
            userName = userName,
            address = address
        )
        return userRepository.save(user)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}