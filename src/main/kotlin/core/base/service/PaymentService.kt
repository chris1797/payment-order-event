package core.base.service

import core.base.domain.order.Order
import core.base.domain.order.OrderStatus
import core.base.domain.payment.Payment
import core.base.domain.user.User
import core.base.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Transactional(readOnly = true)
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    @Transactional(readOnly = false)
    fun processPayment(order: Order, user: User): Order {
        validatePayment(order, user)

        val payment = Payment.of(
            order = order,
            user = user,
        )
        paymentRepository.save(payment)

        // 결제 완료 알람 전송
        sendPaymentNotification(user, payment)

        // 주문 상태 업데이트 후 return
        return order.markAsCompleted()
    }

    private fun sendPaymentNotification(user: User, payment: Payment) {
        println("결제 완료 알람 전송: 사용자 ${user.id}, 결제 ID ${payment.id}")

        // Firebase Cloud Messaging(FCM) 전송 로직
    }

    private fun validatePayment(order: Order, user: User) {
        require(order.user?.id == user.id) { "주문 사용자와 결제 사용자가 일치하지 않습니다" }
        require(order.totalAmount > BigDecimal.ZERO) { "결제 금액은 0보다 커야 합니다" }
    }

}
