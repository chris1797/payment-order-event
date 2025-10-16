package com.msa.order.api

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderEventsTestController(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {

    @GetMapping("/_probe/send")
    fun sendTestMessage(@RequestParam msg: String?): String {
        val message = msg ?: "probe-message" // 전송할 메시지
        val topic = "order-events" // Kafka 토픽 이름

        // 메시지 전송
        kafkaTemplate.send(topic, message)

        return "Sent message to topic '$topic': $message"
    }
}