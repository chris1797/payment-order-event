package core.base.service

import core.base.api.controller.OrderResponse
import core.base.api.request.OrderCreateRequest
import core.base.domain.order.Order
import core.base.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val userService: UserService
) {
    fun getOrders(): List<OrderResponse> =
        orderRepository.findAll().map { OrderResponse.from(it) }

    @Transactional(readOnly = false)
    fun saveOrder(createRequest: OrderCreateRequest): Order {
        validateOrderRequest(createRequest)

        val user = userService.getUserById(createRequest.userId)
        val order = Order.create(
            user = user,
            productName = createRequest.productName,
            quantity = createRequest.quantity,
            totalAmount = createRequest.totalAmount
        )

        return orderRepository.save(order)
    }

    private fun validateOrderRequest(request: OrderCreateRequest) {
        require(request.quantity > 0) { "주문 수량은 0보다 커야 합니다" }
        require(request.totalAmount > BigDecimal.ZERO) { "주문 금액은 0보다 커야 합니다" }
    }
}