package core.carrotkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class CarrotKotlinApplication

fun main(args: Array<String>) {
    runApplication<CarrotKotlinApplication>(*args)
}
