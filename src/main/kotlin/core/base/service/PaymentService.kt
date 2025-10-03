package core.base.service

import core.base.domain.order.Order
import core.base.domain.payment.Payment
import core.base.domain.user.User
import core.base.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    @Transactional(readOnly = false)
    fun processPayment(order: Order, user: User) {
        // 결제 처리 로직 (예: 결제 게이트웨이 연동)
        val payment = Payment.of(
            order = order,
            user = user,
        )
        paymentRepository.save(payment)
    }

}
