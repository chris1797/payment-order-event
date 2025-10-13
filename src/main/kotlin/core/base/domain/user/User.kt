package core.base.domain.user

import core.base.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table


@Entity
@Table(name = "users")
class User(

    @Column(nullable = false)
    val userName: String,

    @Column(nullable = false)
    val address: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: UserStatus = UserStatus.ACTIVE,

): BaseEntity()

enum class UserStatus {
    ACTIVE,
    INACTIVE
}
