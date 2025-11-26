package com.msa.order.infra

import com.msa.order.events.OrderCreated
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.util.Properties
import kotlin.system.measureTimeMillis

@Profile("cloud")
@Component
class KafkaWarmupRunner(
    private val kafkaTemplate: KafkaTemplate<String, com.msa.order.events.OrderCreated>
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val bootstrapServers = "localhost:9092"
    private val schemaRegistryUrl = "http://localhost:8081"
    private val topic = "order-events"


    @EventListener(ApplicationReadyEvent::class)
    fun onReady() {
        val total = measureTimeMillis {
            waitForKafkaCluster(Duration.ofSeconds(30))
            waitForSchemaRegistry(Duration.ofSeconds(15))
        }
        log.info("Kafka Warmup Runner started")
        log.info("Bootstrap Servers: $bootstrapServers")
        log.info("Schema Registry URL: $schemaRegistryUrl")
        // Send a test message to warm up the Kafka connection
        val warmupEvent = OrderCreated.newBuilder()
            .setOrderId("warmup-id")
            .setOrderCode("WARMUP")
            .setTimestamp(System.currentTimeMillis())
            .setEventType("ORDER_CREATED")
            .build()
        kafkaTemplate.send(topic, "warmup-key", warmupEvent)
        log.info("Sent warmup message to topic '$topic'")
    }

    /**
     * 설명 : AdminClient를 사용하여 Kafka 클러스터에 연결을 시도 (클러스터가 준비될 때까지 주기적으로)
     * 타임아웃이 지나면 예외 발생
     */
    private fun waitForKafkaCluster(timeOut: Duration) {
        val end = System.currentTimeMillis() + timeOut.toMillis()
        val props = Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(AdminClientConfig.CLIENT_ID_CONFIG, "order-warmup-admin")
        }

        // 설명 : AdminClient를 사용하여 Kafka 클러스터에 연결을 시도 (클러스터가 준비될 때까지 주기적으로)
        AdminClient.create(props).use { adminClient ->
            while (true) {
                try {
                    val cluster = adminClient.describeCluster().clusterId().get()
                    if (cluster.isNotEmpty()) {
                        log.info("Connected to Kafka cluster with id: $cluster")
                        return
                    }
                } catch (e: Exception) {
                    log.info("Waiting for Kafka cluster to be available...", e)
                }

                if (System.currentTimeMillis() > end) {
                    throw IllegalStateException("Timed out waiting for Kafka cluster to be available")
                }

                Thread.sleep(500)
            }
        }
    }

    /** 설명 : Schema Registry가 HTTP 핑 - 200 OK 면 준비 완료 */
    private fun waitForSchemaRegistry(timeOut: Duration) {
        // 타임아웃 시간 계산
        val end = System.currentTimeMillis() + timeOut.toMillis()

        // Schema Registry의 subjects 엔드포인트 URL
        val pingUrl = "$schemaRegistryUrl/subjects"

        /**
         * 설명 : Schema Registry에 HTTP GET 요청을 보내서 200 OK 응답이 올 때까지 주기적으로 시도
         * 타임아웃이 지나면 예외 발생
         */
        while (true) {
            try {
                val url = URL(pingUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 2000
                    readTimeout = 2000
                }
                try {
                    if (connection.responseCode == HttpStatus.OK.value()) {
                        log.info("Connected to Schema Registry at $schemaRegistryUrl")
                        return
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                log.debug("Waiting for Schema Registry to be available...", e)
            }

            // 타임아웃 체크
            if (System.currentTimeMillis() > end) {
                throw IllegalStateException("Timed out waiting for Schema Registry to be available")
            }

            Thread.sleep(500)
        }
    }
}