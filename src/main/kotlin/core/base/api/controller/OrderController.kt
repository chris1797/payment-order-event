package core.base.api.controller

import core.base.api.request.OrderCreateRequest
import core.base.api.response.OrderResponse
import core.base.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderService: OrderService
) {


    @PostMapping("/order")
    fun createOrder(
        orderRequest: OrderCreateRequest
    ): ResponseEntity<Void>? {
        orderService.saveOrder(orderRequest)
        return ResponseEntity.ok().build()
    }

}