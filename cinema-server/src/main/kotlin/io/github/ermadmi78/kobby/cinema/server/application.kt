package io.github.ermadmi78.kobby.cinema.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Created on 28.02.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}