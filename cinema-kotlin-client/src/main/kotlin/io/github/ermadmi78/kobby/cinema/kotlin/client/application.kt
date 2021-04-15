package io.github.ermadmi78.kobby.cinema.kotlin.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.Actor
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.Film
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.findFilms
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.fetchCountry
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
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
        Auth {
            basic {
                username = "admin"
                password = "admin"
            }
        }
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

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Select country by id")
        // query($arg0: ID!) { country(id: $arg0) { id name } }
        // {arg0=1}
        context.query {
            country(1) {
                // id is required (see @required annotation in schema)
                // name is default (see @default annotation in schema)
            }
        }.country?.also {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Select countries limited by default")
        // query { countries { id name } }
        context.query {
            countries {
                // id is required
                // name is default
            }
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Select film by id")
        // query($arg0: ID!) { film(id: $arg0) { id title countryId } }
        // {arg0=0}
        context.query {
            film(0) {
                // id is required
                // title is default
                // countryId is required
            }
        }.film?.also {
            println("Film id=${it.id}, title=${it.title}")
        }
        println("---------------------------------")

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Select countries unlimited")
        // query($arg0: Int!) { countries(limit: $arg0) { id name } }
        // {arg0=-1}
        context.query {
            countries(limit = -1) {
                // id is required
                // name is default
            }
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }
        println("---------------------------------")

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println(
            "Select films and actors of some country whose names contain the symbol 'd' " +
                    "with related actors and films"
        )
        // query($arg0: ID!, $arg1: String, $arg2: Int!, $arg3: String, $arg4: [String!], $arg5: Int!) { country(id: $arg0) { id name films(title: $arg1) { id title genre countryId actors(limit: $arg2) { id firstName lastName birthday gender countryId country { id name } } } actors(firstName: $arg3) { id fields(keys: $arg4) firstName lastName birthday gender countryId films(limit: $arg5) { id title countryId } } } }
        // {arg0=7, arg1=d, arg2=-1, arg3=d, arg4=[birthday, gender], arg5=-1}
        context.query {
            country(7) {
                // id is required
                // name is default
                films {
                    title = "d" // title is selection argument (see @selection directive in schema)

                    // id is required
                    // title is default
                    genre()
                    // countryId is required
                    actors {
                        limit = -1 // limit is selection argument (see @selection directive in schema)

                        // id is required
                        // firstName is default
                        // lastName is default
                        // birthday is required (see @required directive in schema)
                        gender()
                        // countryId is required
                        country {
                            // id is required
                            // name is default
                        }
                    }
                }
                actors {
                    firstName = "d" // firstName is selection argument (see @selection directive in schema)

                    // id is required
                    fields {
                        keys = listOf(
                            "birthday",
                            "gender"
                        ) // keys is selection argument (see @selection directive in schema)
                    }
                    // firstName is default
                    // lastName is default
                    // birthday is required
                    gender()
                    // countryId is required
                    films {
                        limit = -1 // limit is selection argument (see @selection directive in schema)

                        // id is required
                        // title is default
                        // countryId is required
                    }
                }
            }
        }.country!!.also { country ->
            println("Country: id=${country.id}, name=${country.name}")
            country.films.forEach { film ->
                println("Film: id=${film.id}, title='${film.title}', genre=${film.genre}")
                val actors = film.actors.joinToString {
                    "${it.firstName} ${it.lastName} (${it.gender.name.toLowerCase()}) from ${it.country.name}"
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

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Let try to select interfaces")
        // query($arg0: String!) { taggable(tag: $arg0) { id tags { value } __typename ... on Film { title genre countryId } ... on Actor { firstName lastName birthday gender countryId country { id name } } } }
        // {arg0=best}
        context.query {
            taggable("best") {
                // id is default
                tags {
                    value()
                }
                // __typename generated by Kobby
                __onFilm {
                    // title is default
                    genre()
                    // countryId is required
                }
                __onActor {
                    // firstName is default
                    // lastName is default
                    // birthday is required
                    gender()
                    // countryId is required
                    country {
                        // id is required
                        // name is default
                    }
                }
            }
        }.taggable.forEach { cur ->
            val tags = cur.tags.joinToString { it.value }
            when (cur) {
                is Film -> {
                    println("Film[$tags]: id=${cur.id}, title='${cur.title}' genre=${cur.genre}")
                }
                is Actor -> {
                    println(
                        "Actor[$tags]: ${cur.firstName} ${cur.lastName} (${cur.gender.name.toLowerCase()}) " +
                                "from ${cur.country.name}"
                    )
                }
                else -> error("Invalid algorithm")
            }
        }
        println("---------------------------------")

        //**************************************************************************************************************

        println()
        println("---------------------------------")
        println("Let try to select unions")
        // query($arg0: ID!) { country(id: $arg0) { id native { __typename ... on Film { id title genre countryId } ... on Actor { id firstName lastName birthday gender countryId country { id name } } } } }
        // {arg0=17}
        context.query {
            country(17) {
                __minimize() // switch off defaults to minimize query
                // id is required
                native {
                    // __typename generated by Kobby
                    __onFilm {
                        // id is required
                        // title is default
                        genre()
                        // countryId is required
                    }
                    __onActor {
                        // id is required
                        // firstName is default
                        // lastName is default
                        // birthday is required
                        gender()
                        // countryId is required
                        country {
                            // id is required
                            // name is default
                        }
                    }
                }
            }
        }.country!!.native.forEach { cur ->
            when (cur) {
                is Film -> {
                    println("Film: id=${cur.id}, title='${cur.title}' genre=${cur.genre}")
                }
                is Actor -> {
                    println(
                        "Actor: ${cur.firstName} ${cur.lastName} (${cur.gender.name.toLowerCase()}) " +
                                "from ${cur.country.name}"
                    )
                }
                else -> error("Invalid algorithm")
            }
        }
        println("---------------------------------")

        //**************************************************************************************************************
        //                                                 Sugar API
        //**************************************************************************************************************
        println()
        println("---------------------------------")
        println("Let try our sugar API")

        println()
        println("Fetch country by id")
        val country = context.fetchCountry(7)
        println("Country: id=${country.id} name='${country.name}'")

        println()
        println("Find all country films")
        val films = country.findFilms {
            limit = -1
            genre()
        }

        films.forEach {
            println("Film: id=${it.id}, title='${it.title}' genre=${it.genre}")
        }
    }
}