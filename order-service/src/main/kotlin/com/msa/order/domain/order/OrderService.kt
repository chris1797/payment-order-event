package com.msa.order.domain.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.order.domain.outbox.Outbox
import com.msa.order.domain.outbox.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val TOPIC = "order-events"
    }

    @Transactional
    fun createOrder(orderCode: String): Order {
        // 1. 주문 저장
        val order = orderRepository.save(Order(orderCode))

        // 2. Outbox 이벤트 저장 (같은 트랜잭션)
        val eventPayload = mapOf(
            "orderId" to order.id.toString(),
            "orderCode" to order.orderCode,
            "timestamp" to System.currentTimeMillis(),
            "eventType" to "ORDER_CREATED"
        )

        val outbox = Outbox(
            aggregateType = "Order",
            aggregateId = order.id.toString(),
            eventType = "ORDER_CREATED",
            payload = objectMapper.writeValueAsString(eventPayload),
            topic = TOPIC
        )
        outboxRepository.save(outbox)

        return order
    }

    fun findByOrderCode(orderCode: String): Order? {
        return orderRepository.findByOrderCode(orderCode)
    }
}