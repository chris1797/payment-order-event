package core.base.api.controller

import core.base.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping("/orders")
    fun getOrders(): List<OrderResponse> = orderService.getOrders()

}