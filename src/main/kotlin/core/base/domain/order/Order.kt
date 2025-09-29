package core.base.domain.order

import core.base.domain.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "orders")
class Order(

    @Column(nullable = false)
    val productName: String,

    @Column(nullable = false)
    val quantity: Int = 1,

    @Column(nullable = false, precision = 15, scale = 2)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING

): BaseEntity()