package core.base.domain.order

import core.base.domain.user.User
import java.lang.reflect.Field
import java.math.BigDecimal

object OrderFixture {
    /**
     * 필요한 것들
     * - userId, 주문(상품, 개수, 총액), 주소
     */
    fun createOrder(
        id: Long? = null,
        productName: String,
        quantity: Int = 1,
        totalAmount: BigDecimal = BigDecimal("100.00"),
    ): Order {
        val order = Order(
            productName = productName,
            quantity = quantity,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            user = User(userName = "Test User", address = "123 Test St"),
            address = "123 Test St"
        )

        // ID 설정 (테스트용)
        id?.let { setId(order, it) }

        return order
    }

    fun createOrders(): List<Order> {
        return listOf(
            createOrder(1L, "product1", 1, BigDecimal("100.00")),
            createOrder(2L, "product2", 1, BigDecimal("200.50")),
            createOrder(3L, "product3", 1, BigDecimal("150.75"))
        )
    }

    private fun setId(order: Order, id: Long) {
        val idField: Field = order.javaClass.superclass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(order, id)
    }
}