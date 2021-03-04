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

    override fun run(vararg args: String?): Unit = runBlocking {
        val context = cinemaContextOf(CinemaKtorAdapter(httpClient))

        println()
        println("---------------------------------")
        println("Select country by id")
        context.query {
            country(1)
        }.country?.also {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")

        println()
        println("---------------------------------")
        println("Select countries limited by default")
        context.query {
            countries()
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")


        println()
        println("---------------------------------")
        println("Select countries unlimited")
        context.query {
            countries(limit = -1)
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")

        println()
        println("---------------------------------")
        println(
            "Select films and actors of some country whose names contain the symbol 'd' " +
                    "with related actors and films"
        )
        context.query {
            country(7) {
                films {
                    title = "d"

                    genre()
                    actors {
                        limit = -1
                        gender()
                        country()
                    }
                }
                actors {
                    firstName = "d"

                    fields {
                        keys = listOf("birthday", "gender")
                    }
                    gender()
                    films {
                        limit = -1
                    }
                }
            }
        }.country!!.also { country ->
            println("Country: id=${country.id}, name=${country.name}")
            country.films.forEach { film ->
                println("Film: id=${film.id}, title='${film.title}', genre=${film.genre}")
                val actors = film.actors.joinToString {
                    "${it.firstName} ${it.lastName} (${it.gender.name.toLowerCase()}) of ${it.country.name}"
                }
                println("    actors: $actors")
            }
            country.actors.forEach { actor ->
                println(
                    "Actor: id=${actor.id}, firstName='${actor.firstName}', lastName='${actor.lastName}', " +
                            "birthday=${actor.birthday}, gender=${actor.gender}, fields=${actor.fields}"
                )
                println("    films: ${actor.films.joinToString { it.title }}")
            }
        }
        println("---------------------------------")

//        println()
//        println("---------------------------------")
//        println("Let try to select interfaces")
//        context.query {
//            taggable("best") {
//                __onFilm {
//                    genre()
//                }
//                __onActor {
//                    gender()
//                }
//            }
//        }
    }
}