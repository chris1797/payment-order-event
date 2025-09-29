package core.base.domain.order

import core.base.service.OrderService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderService(orderRepository)

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
}