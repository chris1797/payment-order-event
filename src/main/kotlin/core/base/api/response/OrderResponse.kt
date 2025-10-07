package core.base.api.response

import core.base.domain.order.Order
import core.base.domain.order.OrderStatus
import core.base.domain.user.User
import jakarta.persistence.Id
import java.math.BigDecimal

// 주문 응답 DTO
data class OrderResponse(
    val id: Long,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val userId: Long,
    val userName: String,
) {
    companion object {
        fun from(order: Order): OrderResponse = OrderResponse(
            id = order.id!!,
            totalAmount = order.totalAmount,
            status = order.status,
            userId = order.user!!.id!!,
            userName = order.user!!.userName,
        )
    }
}