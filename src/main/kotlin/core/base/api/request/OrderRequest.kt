package core.base.api.request

import core.base.domain.order.Order
import core.base.domain.user.User
import java.math.BigDecimal

data class OrderCreateRequest(
    val userId: Long,
    val productName: String,
    val quantity: Int,
    val totalAmount: BigDecimal,
) {
    fun toEntity(user: User): Order {
        return Order(
            user = user,
            address = user.address,
            productName = productName,
            quantity = quantity,
            totalAmount = totalAmount,
        )
    }
}