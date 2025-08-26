package core.base

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class BaseApplication

fun main(args: Array<String>) {
    runApplication<BaseApplication>(*args)
}
