package core.base.domain.order

import java.lang.reflect.Field
import java.math.BigDecimal

object OrderFixture {
    fun createOrder(
        id: Long? = null,
        totalAmount: BigDecimal = BigDecimal("100.00"),
        status: OrderStatus = OrderStatus.PENDING
    ): Order {
        val order = Order(
            totalAmount = totalAmount,
            status = status
        )

        // ID 설정 (테스트용)
        id?.let { setId(order, it) }

        return order
    }

    fun createOrders(): List<Order> {
        return listOf(
            createOrder(1L, BigDecimal("100.00"), OrderStatus.PENDING),
            createOrder(2L, BigDecimal("200.50"), OrderStatus.PAID),
            createOrder(3L, BigDecimal("150.75"), OrderStatus.CANCELLED)
        )
    }

    private fun setId(order: Order, id: Long) {
        val idField: Field = order.javaClass.superclass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(order, id)
    }
}