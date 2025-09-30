package core.base.service

import core.base.api.controller.OrderResponse
import core.base.api.request.OrderCreateRequest
import core.base.domain.order.OrderRepository
import core.base.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val userService: UserService
) {
    fun getOrders(): List<OrderResponse> =
        orderRepository.findAll().map { OrderResponse.from(it) }

    @Transactional(rollbackFor = [Exception::class])
    fun createOrder(createRequest: OrderCreateRequest) {
        val user = userService.getUserById(createRequest.userId)

        val order = createRequest.toEntity(user.id, user.address)

        TODO("Not yet implemented")


    }
}