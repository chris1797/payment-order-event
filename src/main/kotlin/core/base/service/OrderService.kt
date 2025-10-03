package core.base.service

import core.base.api.response.OrderResponse
import core.base.api.request.OrderCreateRequest
import core.base.domain.order.Order
import core.base.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Service
@Transactional(readOnly = true)
class OrderService(
    private val userService: UserService,
    private val paymentService: PaymentService,
    private val orderRepository: OrderRepository,
) {


    fun getOrders(): List<OrderResponse> =
        orderRepository.findAll().map { OrderResponse.from(it) }

    /**
     * 주문 생성
     */
    @Transactional(readOnly = false)
    fun saveOrder(createRequest: OrderCreateRequest): Order {
        validateOrderRequest(createRequest)

        // 사용자 조회 후 주문 생성
        val user = userService.getUserById(createRequest.userId)
        val order = Order.of(
            user = user,
            productName = createRequest.productName,
            quantity = createRequest.quantity,
            totalAmount = createRequest.totalAmount
        )

        paymentService.processPayment(order, user)

        return orderRepository.save(order)
    }

    /**
     * 주문 요청 유효성 검사
     */
    private fun validateOrderRequest(request: OrderCreateRequest) {
        require(request.quantity > 0) { "주문 수량은 0보다 커야 합니다" }
        require(request.totalAmount > BigDecimal.ZERO) { "주문 금액은 0보다 커야 합니다" }
    }
}