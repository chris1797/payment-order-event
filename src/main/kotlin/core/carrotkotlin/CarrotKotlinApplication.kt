package core.carrotkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CarrotKotlinApplication

fun main(args: Array<String>) {
    runApplication<CarrotKotlinApplication>(*args)
}
