package com.msa.user.api.dto

import com.msa.user.domain.User
import com.msa.user.domain.UserStatus

data class UserCreateRequest(
    val userName: String,
    val address: String
)

data class UserResponse(
    val id: Long,
    val userName: String,
    val address: String,
    val status: UserStatus
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                userName = user.userName,
                address = user.address,
                status = user.status
            )
        }
    }
}