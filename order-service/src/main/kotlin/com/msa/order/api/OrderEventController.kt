package com.msa.order.api

import com.msa.order.domain.order.Order
import com.msa.order.domain.order.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderEventController(
    private val orderService: OrderService,
) {

    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(request.orderCode)
        return ResponseEntity.ok(OrderResponse.from(order))
    }

    @GetMapping
    fun getOrder(orderCode: String): ResponseEntity<OrderResponse> {
        val order = orderService.findByOrderCode(orderCode)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(OrderResponse.from(order))
    }
}

data class CreateOrderRequest(
    val orderCode: String
)

data class OrderResponse(
    val id: Long,
    val orderCode: String,
) {
    companion object {
        fun from(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id!!,
                orderCode = order.orderCode
            )
        }
    }
}