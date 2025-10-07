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


    /**
     * 주문 생성
     */
    @Transactional(readOnly = false)
    fun saveOrder(createRequest: OrderCreateRequest): OrderResponse {
        createRequest.validateOrderRequest()

        // 사용자 조회 후 주문 생성
        val user = userService.getUserById(createRequest.userId)
        val order = Order.of(
            user = user,
            productName = createRequest.productName,
            quantity = createRequest.quantity,
            totalAmount = createRequest.totalAmount
        )

        val savedOrder = orderRepository.save(order)
        val payedOrder = paymentService.processPayment(savedOrder, user)

        return OrderResponse.from(payedOrder)
    }

}