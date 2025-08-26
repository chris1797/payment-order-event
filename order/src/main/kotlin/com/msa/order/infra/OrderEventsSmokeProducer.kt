package com.msa.order.infra

import org.springframework.boot.CommandLineRunner
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventsSmokeProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
): CommandLineRunner {

    override fun run(vararg args: String?) {
        val topic = "order-events" // Kafka 토픽 이름
        val message = "Order service is up and running!" // 전송할 메시지

        // 메시지 전송
        kafkaTemplate.send(topic, message)
            .whenComplete { _, ex ->
                if (ex != null) {
                    println("❌ Failed to send message: ${ex.message}")
                } else {
                    println("✅ Successfully sent message to topic '$topic': $message")
                }
            }
    }
}