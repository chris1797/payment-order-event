package core.base.domain.user

import core.base.domain.common.BaseEntity
import core.base.domain.order.Order
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    val orders: MutableList<Order> = mutableListOf()

): BaseEntity() {

}

enum class UserStatus {
    ACTIVE,
    INACTIVE
}
