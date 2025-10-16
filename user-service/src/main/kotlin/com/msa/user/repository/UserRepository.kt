package com.msa.user.repository

import com.msa.user.domain.User
import com.msa.user.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUserName(userName: String): User?
    fun findByStatus(status: UserStatus): List<User>
    fun existsByUserName(userName: String): Boolean
}