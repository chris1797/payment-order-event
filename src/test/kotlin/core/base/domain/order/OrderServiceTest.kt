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

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val userService = mockk<UserService>()
    private val paymentService = mockk<PaymentService>()
    private val orderService = OrderService(userService, paymentService, orderRepository)

    /**
     * 주문 로직에서 단위테스트
     * 1. request의 검증
     * 2. order 저장 후 반환 검증
     * 3. 결제 단계 검증
     */

    @Test
    fun `OrderRequest 의 수량(quantity) 이 0개 일 때 exception 발생`() {
        val testQuantity = 0

        // given & when
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = testQuantity,
            totalAmount = BigDecimal("200.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            request.validateOrderRequest()
        }

        assertEquals(OrderCreateRequest.ExceptionMessage.QUANTITY_VALIDATION.name, exception.message)
    }

    @Test
    fun `OrderRequest 의 총 금액(totalAmount) 가 0원 일 때 exception 발생`() {
        val testTotalAmount = BigDecimal.ZERO

        // given & when
        val request = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = testTotalAmount
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            request.validateOrderRequest()
        }

        assertEquals(OrderCreateRequest.ExceptionMessage.TOTAL_AMOUNT_VALIDATION.name, exception.message)
    }


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

}