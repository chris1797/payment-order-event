package core.base.domain.payment

import core.base.repository.PaymentRepository
import core.base.service.PaymentService
import io.mockk.mockk

class PaymentServiceTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentService = PaymentService(paymentRepository)


}