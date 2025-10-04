package core.base.domain.order

import core.base.api.request.OrderCreateRequest
import core.base.domain.user.User
import core.base.repository.PaymentRepository
import core.base.service.OrderService
import core.base.service.PaymentService
import core.base.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val paymentRepository = mockk<PaymentRepository>()
    private val userService = mockk<UserService>()
    private val paymentService = mockk<PaymentService>()
    private val orderService = OrderService(userService, paymentService, orderRepository)



    @Test
    fun `saveOrder() 정상적인 주문 생성`() {
        // given
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 2,
            totalAmount = BigDecimal("200.00")
        )
        val user = User(userName = "Test User", address = "Test Address")
        val savedOrder = OrderFixture.createOrder(
            id = 1L,
            productName = request.productName,
            quantity = request.quantity,
            totalAmount = request.totalAmount
        )
        val completedOrder = OrderFixture.createOrder(
            id = 1L,
            productName = request.productName,
            quantity = request.quantity,
            totalAmount = request.totalAmount
        ).markAsCompleted()

        every { userService.getUserById(1L) } returns user
        every { orderRepository.save(any()) } returns savedOrder
        every { paymentService.processPayment(savedOrder, user) } returns completedOrder

        // when
        val orderResponse = orderService.saveOrder(request)

        // then
        verify(exactly = 1) { userService.getUserById(1L) }
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { paymentService.processPayment(savedOrder, user) }

        // 비즈니스 로직 검증: 주문 생성 및 결제 완료
        assertAll(
            { assertEquals(1L, orderResponse.id) },
            { assertEquals(request.totalAmount, orderResponse.totalAmount) },
            { assertEquals(OrderStatus.COMPLETED, orderResponse.status) },
            { assertEquals(user.id, orderResponse.userId) },
            { assertEquals(user.userName, orderResponse.userName) }
        )
    }

    @Test
    fun `saveOrder() 수량이 0 이하일 때 예외 발생`() {
        // given
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 0,
            totalAmount = BigDecimal("100.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(request)
        }
        assertEquals("주문 수량은 0보다 커야 합니다", exception.message)
    }

    @Test
    fun `saveOrder() 수량이 음수일 때 예외 발생`() {
        // given
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = -5,
            totalAmount = BigDecimal("100.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(request)
        }
        assertEquals("주문 수량은 0보다 커야 합니다", exception.message)
    }

    @Test
    fun `saveOrder() 금액이 0일 때 예외 발생`() {
        // given
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal.ZERO
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(request)
        }
        assertEquals("주문 금액은 0보다 커야 합니다", exception.message)
    }

    @Test
    fun `saveOrder() 금액이 음수일 때 예외 발생`() {
        // given
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal("-100.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(request)
        }
        assertEquals("주문 금액은 0보다 커야 합니다", exception.message)
    }
}