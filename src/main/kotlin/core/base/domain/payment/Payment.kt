package core.base.domain.payment

import core.base.domain.common.BaseEntity
import core.base.domain.order.Order
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "payments")
class Payment(

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Order,

    @Column(nullable = false, precision = 15, scale = 2) // BigDecimal(15, 2)
    val amount: BigDecimal,


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.INITIATED,

    // 멱등, 추적용 PG사 transaction id (개발 환경에서는 generateRandomId() 등으로 생성)
    @Column(name = "tx_id", nullable = false, unique = true)
    val txId: String

    ): BaseEntity()