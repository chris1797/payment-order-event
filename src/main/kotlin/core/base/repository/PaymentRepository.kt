package core.base.repository

import core.base.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    fun findByTxId(txId: String): Payment?

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    fun findByOrderId(orderId: Long): Payment?
}
