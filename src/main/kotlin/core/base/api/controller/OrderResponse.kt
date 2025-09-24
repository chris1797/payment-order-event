package core.base.api.controller

import core.base.domain.order.Order
import java.math.BigDecimal

// 주문 응답 DTO
data class OrderResponse(
    val id: Long,
    val totalAmount: BigDecimal,
    val status: String
) {
    companion object {
        fun from(order: Order): OrderResponse = OrderResponse(
            id = order.id!!,
            totalAmount = order.totalAmount,
            status = order.status.name
        )
    }
}

