package core.base.domain.payment.integration

import core.base.config.IntegrationTestBase
import core.base.domain.order.Order
import core.base.domain.order.OrderRepository
import core.base.domain.payment.Payment
import core.base.domain.payment.PaymentMethod
import core.base.domain.payment.PaymentStatus
import core.base.domain.user.User
import core.base.domain.user.UserFixture
import core.base.repository.PaymentRepository
import core.base.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class PaymentServiceTest : IntegrationTestBase() {

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private lateinit var testOrder: Order

    @BeforeEach
    fun setUp() {
        testUser = UserFixture.createUser(name = "Payment Test User")
        userRepository.save(testUser)

        testOrder = Order.of(
            user = testUser,
            productName = "Test Product",
            quantity = 2,
            totalAmount = BigDecimal("300.00")
        )
        orderRepository.save(testOrder)
    }

    @Test
    @DisplayName("Payment 생성 - Payment가 성공적으로 DB에 저장됨")
    fun `Payment 생성 성공 테스트`() {
        // given
        val payment = Payment.of(testOrder, testUser)

        // when
        val savedPayment = paymentRepository.save(payment)

        // then
        Assertions.assertNotNull(savedPayment.id)
        Assertions.assertEquals(testOrder.id, savedPayment.order.id)
        Assertions.assertEquals(testUser.id, savedPayment.user.id)
        Assertions.assertEquals(BigDecimal("300.00"), savedPayment.amount)
        Assertions.assertEquals(PaymentStatus.INITIATED, savedPayment.status)
        Assertions.assertEquals(PaymentMethod.CREDIT_CARD, savedPayment.method)
        Assertions.assertNotNull(savedPayment.txId)
    }

    @Test
    @DisplayName("Payment 조회 - txId로 Payment 조회 성공")
    fun `txId로 Payment 조회 테스트`() {
        // given
        val payment = Payment.of(testOrder, testUser)
        val savedPayment = paymentRepository.save(payment)

        // when
        val foundPayment = paymentRepository.findByTxId(savedPayment.txId)

        // then
        Assertions.assertNotNull(foundPayment)
        Assertions.assertEquals(savedPayment.id, foundPayment?.id)
        Assertions.assertEquals(savedPayment.txId, foundPayment?.txId)
    }

    @Test
    @DisplayName("Payment 조회 - orderId로 Payment 조회 성공")
    fun `orderId로 Payment 조회 테스트`() {
        // given
        val payment = Payment.of(testOrder, testUser)
        val savedPayment = paymentRepository.save(payment)

        // when
        val foundPayment = paymentRepository.findByOrderId(testOrder.id!!)

        // then
        Assertions.assertNotNull(foundPayment)
        Assertions.assertEquals(savedPayment.id, foundPayment?.id)
        Assertions.assertEquals(testOrder.id, foundPayment?.order?.id)
    }

    @Test
    @DisplayName("여러 Payment 생성 - 각각 고유한 txId를 가짐")
    fun `여러 Payment 생성 테스트`() {
        // given
        val order2 = Order.of(
            user = testUser,
            productName = "Product 2",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )
        val savedOrder2 = orderRepository.save(order2)

        val payment1 = Payment.of(testOrder, testUser)
        Thread.sleep(1) // txId 고유성 보장
        val payment2 = Payment.of(savedOrder2, testUser)

        // when
        val savedPayment1 = paymentRepository.save(payment1)
        val savedPayment2 = paymentRepository.save(payment2)

        // then
        Assertions.assertNotEquals(savedPayment1.txId, savedPayment2.txId)
        Assertions.assertNotEquals(savedPayment1.order.id, savedPayment2.order.id)
        Assertions.assertEquals(testUser.id, savedPayment1.user.id)
        Assertions.assertEquals(testUser.id, savedPayment2.user.id)
    }

    @Test
    @DisplayName("다양한 결제 상태로 Payment 생성")
    fun `다양한 결제 상태 Payment 생성 테스트`() {
        // given & when
        val approvedPayment = Payment(
            order = testOrder,
            user = testUser,
            amount = BigDecimal("300.00"),
            status = PaymentStatus.APPROVED,
            txId = "TX_APPROVED_${System.currentTimeMillis()}"
        )
        val savedApprovedPayment = paymentRepository.save(approvedPayment)

        // then
        Assertions.assertEquals(PaymentStatus.APPROVED, savedApprovedPayment.status)
        Assertions.assertNotNull(savedApprovedPayment.id)
    }

    @Test
    @DisplayName("다양한 결제 수단으로 Payment 생성")
    fun `다양한 결제 수단 Payment 생성 테스트`() {
        // given
        val order2 = Order.of(
            user = testUser,
            productName = "Cash Product",
            quantity = 1,
            totalAmount = BigDecimal("50.00")
        )
        orderRepository.save(order2)

        // when
        val cashPayment = Payment(
            order = order2,
            user = testUser,
            amount = BigDecimal("50.00"),
            method = PaymentMethod.CASH,
            txId = "TX_CASH_${System.currentTimeMillis()}"
        )
        val savedCashPayment = paymentRepository.save(cashPayment)

        // then
        Assertions.assertEquals(PaymentMethod.CASH, savedCashPayment.method)
        Assertions.assertNotNull(savedCashPayment.id)
    }
}