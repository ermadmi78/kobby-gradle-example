package io.github.ermadmi78.kobby.cinema.kotlin.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@SpringBootApplication
class Application : CommandLineRunner {
    private val httpClient = HttpClient {
        expectSuccess = true
        install(JsonFeature) {
            serializer = JacksonSerializer() {
                registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                registerModule(JavaTimeModule())
            }
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            url { takeFrom("http://localhost:8080/graphql") }
        }
    }

    override fun run(vararg args: String?) = runBlocking {
        val context = cinemaContextOf(CinemaKtorAdapter(httpClient))

        println()
        println("Select country by id")
        context.query {
            country(1)
        }.country?.also {
            println("id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")


//        println()
//        println("Select countries unlimited")
//        context.query {
//            countries(limit = -1)
//        }.countries.forEach {
//            println("id=${it.id}, name=${it.name}")
//        }
//        println("---------------------------------")
    }
}