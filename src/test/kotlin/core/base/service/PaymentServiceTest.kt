package core.base.service

import core.base.domain.order.OrderRepository
import core.base.repository.PaymentRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*

class PaymentServiceTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentService = PaymentService(paymentRepository)


}