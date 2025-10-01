package core.base.domain.order

import core.base.api.request.OrderCreateRequest
import core.base.domain.user.User
import core.base.service.OrderService
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
    private val userService = mockk<UserService>()
    private val orderService = OrderService(orderRepository, userService)

    @Test
    fun `getOrders() should return list of OrderResponse from repository`() {
        // given
        val orders = OrderFixture.createOrders()
        every { orderRepository.findAll() } returns orders

        // when
        val result = orderService.getOrders()

        // then
        verify { orderRepository.findAll() }

        assertAll(
            { assertEquals(1L, result[0].id) },
            { assertEquals(2L, result[1].id) },
            { assertEquals(3L, result[2].id) },
            { assertTrue(result.all { it.status == OrderStatus.PENDING }) }, // 모든 주문 상태가 PENDING인지 확인

        )
    }

    @Test
    fun `getOrders() 에서 주문이 없을 때 빈 리스트를 반환하는지 검증`() {
        // given
        every { orderRepository.findAll() } returns emptyList()

        // when
        val result = orderService.getOrders()

        // then
        // 리스트가 비어있는지 검증
        verify { orderRepository.findAll() }
        assertEquals(0, result.size)
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

        every { userService.getUserById(1L) } returns user
        every { orderRepository.save(any()) } returns savedOrder

        // when
        val result = orderService.saveOrder(request)

        // then
        verify { userService.getUserById(1L) }
        verify { orderRepository.save(any()) }
        assertEquals(savedOrder.id, result.id)
        assertEquals(savedOrder.productName, result.productName)
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