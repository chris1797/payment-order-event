package com.msa.order.infra.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.order.domain.outbox.OutboxRepository
import com.msa.order.domain.outbox.OutboxStatus
import com.msa.order.events.OrderCreated
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, OrderCreated>,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    @Transactional
    fun publishPendingEvents() {
        val pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)

        pendingEvents.forEach { outbox ->
            try {
                val payload = objectMapper.readValue(outbox.payload, Map::class.java)

                val event = OrderCreated.newBuilder()
                    .setOrderId(payload["orderId"] as String)
                    .setOrderCode(payload["orderCode"] as String)
                    .setTimestamp(payload["timestamp"] as Long)
                    .setEventType(payload["eventType"] as String)
                    .build()

                kafkaTemplate.send(outbox.topic, outbox.aggregateId, event)
                    .whenComplete { _, ex ->
                        if (ex == null) {
                            log.info("Published event: ${outbox.eventType} for ${outbox.aggregateId}")
                        } else {
                            log.error("Failed to publish event: ${outbox.eventType}", ex)
                        }
                    }

                outbox.markAsProcessed()
                outboxRepository.save(outbox)

            } catch (e: Exception) {
                log.error("Error processing outbox event: ${outbox.id}", e)
                outbox.markAsFailed()
                outboxRepository.save(outbox)
            }
        }
    }
}