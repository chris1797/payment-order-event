package core.base.domain.payment

import core.base.domain.common.BaseEntity
import core.base.domain.order.Order
import core.base.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "payments")
class Payment(

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, precision = 15, scale = 2) // BigDecimal(15, 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.INITIATED,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: PaymentMethod = PaymentMethod.CREDIT_CARD,

    // 멱등, 추적용 PG사 transaction id (개발 환경에서는 generateRandomId() 등으로 생성)
    @Column(name = "tx_id", nullable = false, unique = true)
    val txId: String

): BaseEntity() {
    companion object {
        fun of(order: Order, user: User): Payment {
            return Payment(
                order = order,
                user = user,
                amount = order.totalAmount,
                txId = "TX${System.currentTimeMillis()}" // 예시: 실제로는 PG사에서 받은 ID 사용
            )
        }
    }
}


enum class PaymentStatus {
    INITIATED,
    APPROVED,
    DECLINED,
}

enum class PaymentMethod {
    CREDIT_CARD,
    CASH,
}