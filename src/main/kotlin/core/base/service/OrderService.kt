package core.base.service

import core.base.api.controller.OrderResponse
import core.base.domain.order.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun getOrders(): List<OrderResponse> =
        orderRepository.findAll().map { OrderResponse.from(it) }
}