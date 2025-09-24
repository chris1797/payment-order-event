package core.base.domain.order

import core.base.service.OrderService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import kotlin.test.assertEquals

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderService(orderRepository)

    @Test
    fun `getOrders should return list of OrderResponse from repository`() {
        // given
        val orders = OrderFixture.createOrders()
        every { orderRepository.findAll() } returns orders

        // when
        val result = orderService.getOrders()

        // then
        verify { orderRepository.findAll() }

        assertAll(
            { assertEquals(3, result.size) },
            { assertEquals(1L, result[0].id) },
            { assertEquals(BigDecimal("100.00"), result[0].totalAmount) },
            { assertEquals("PENDING", result[0].status) },
            { assertEquals(2L, result[1].id) },
            { assertEquals(BigDecimal("200.50"), result[1].totalAmount) },
            { assertEquals("PAID", result[1].status) },
            { assertEquals(3L, result[2].id) },
            { assertEquals(BigDecimal("150.75"), result[2].totalAmount) },
            { assertEquals("CANCELLED", result[2].status) }
        )
    }

    @Test
    fun `getOrders should return empty list when no orders exist`() {
        // given
        every { orderRepository.findAll() } returns emptyList()

        // when
        val result = orderService.getOrders()

        // then
        verify { orderRepository.findAll() }
        assertEquals(0, result.size)
    }
}