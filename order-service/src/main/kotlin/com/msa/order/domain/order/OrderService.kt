package com.msa.order.domain.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.order.domain.outbox.Outbox
import com.msa.order.domain.outbox.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val TOPIC = "order-events"
    }

    /**
     * 주문 생성 및 Outbox 이벤트 저장
     */
    @Transactional
    fun createOrder(orderCode: String): Order {
        // 1. 주문 정보 db 저장
        val order = orderRepository.save(Order(orderCode))

        // 2. Outbox 이벤트 저장 (같은 트랜잭션 내에서)
        val eventPayload = mapOf(
            "orderId" to order.id.toString(),
            "orderCode" to order.orderCode,
            "timestamp" to System.currentTimeMillis(),
            "eventType" to "ORDER_CREATED"
        )

        // 3. Outbox 테이블에 이벤트 저장
        val outbox = Outbox(
            aggregateType = "Order",
            aggregateId = order.id.toString(),
            eventType = "ORDER_CREATED",
            payload = objectMapper.writeValueAsString(eventPayload), // Map -> JSON 문자열로 변환
            topic = TOPIC
        )
        outboxRepository.save(outbox)

        return order
    }

    /**
     * 주문 코드로 주문 조회
     */
    fun findByOrderCode(orderCode: String): Order? {
        return orderRepository.findByOrderCode(orderCode)
    }
}