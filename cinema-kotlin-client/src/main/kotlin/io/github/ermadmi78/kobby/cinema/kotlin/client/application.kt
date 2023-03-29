package io.github.ermadmi78.kobby.cinema.kotlin.client

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.adapter.ktor.CinemaCompositeKtorAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.adapter.ktor.CinemaSimpleKtorAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorInput
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmInput
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate

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
    override fun run(vararg args: String?): Unit = runBlocking {
        // See more details about the Kobby customization directives
        // (@default, @required, @primaryKey and @selection) here:
        // https://github.com/ermadmi78/kobby/wiki/Client-DSL-Customization

        // Simple adapter does not support GraphQL subscriptions - so we can try only queries and mutations
        simpleKtorAdapterExample()

        // Composite adapter supports GraphQL subscriptions, queries and mutation - let try it
        compositeKtorAdapterExample()
    }

    //******************************************************************************************************************
    //                                           Simple Adapter
    //******************************************************************************************************************
    private suspend fun simpleKtorAdapterExample() {
        val client = HttpClient(CIO) {
            Auth {
                basic {
                    credentials {
                        BasicAuthCredentials("admin", "admin")
                    }
                    sendWithoutRequest { true }
                }
            }
            install(ContentNegotiation) {
                json(cinemaJson)
            }
        }

        val context = cinemaContextOf(
            CinemaSimpleKtorAdapter(client, "http://localhost:8080/graphql") {
                println(">> ${it.query}")
                println(">> ${it.variables}")
            }
        )

        //**************************************************************************************************************

        println()
        println()
        println("##################################################################")
        println("##            Simple Ktor Adapter Example                       ##")
        println("##################################################################")
        println()
        println("******************************************************************")
        println("**                Select country by id                          **")
        println("******************************************************************")
        println()

        // query($arg0: ID!) { country(id: $arg0) { id name } }
        // {arg0=1}
        context.query {
            country(1) {
                // id is primary key (see @primaryKey directive in schema)
                // name is default (see @default directive in schema)
            }
        }.country?.also {
            println("Country id=${it.id}, name=${it.name}")
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**           Select countries limited by default                **")
        println("******************************************************************")
        println()

        // query { countries { id name } }
        context.query {
            countries {
                // id is primary key
                // name is default
            }
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**                   Select film by id                          **")
        println("******************************************************************")
        println()

        // query($arg0: ID!) { film(id: $arg0) { id title countryId } }
        // {arg0=0}
        context.query {
            film(0) {
                // id is primary key
                // title is default
                // countryId is required
            }
        }.film?.also {
            println("Film id=${it.id}, title=${it.title}")
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**              Select countries unlimited                      **")
        println("******************************************************************")
        println()

        // query($arg0: Int!) { countries(limit: $arg0) { id name } }
        // {arg0=-1}
        context.query {
            countries(limit = -1) {
                // id is primary key
                // name is default
            }
        }.countries.forEach {
            println("Country id=${it.id}, name=${it.name}")
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**          Select films and actors of some country             **")
        println("**            whose names contain the symbol 'd'                **")
        println("**              with related actors and films                   **")
        println("******************************************************************")
        println()

        // query($arg0: ID!, $arg1: String, $arg2: Int!, $arg3: String, $arg4: Date, $arg5: [String!], $arg6: Int!) { country(id: $arg0) { id name films(title: $arg1) { id title genre countryId actors(limit: $arg2) { id firstName lastName birthday gender countryId country { id name } } } actors(firstName: $arg3, birthdayFrom: $arg4) { id fields(keys: $arg5) firstName lastName birthday gender countryId films(limit: $arg6) { id title countryId } } } }
        // {arg0=7, arg1=d, arg2=-1, arg3=d, arg4=1970-01-01, arg5=[birthday, gender], arg6=-1}
        context.query {
            country(7) {
                // id is primary key
                // name is default
                films {
                    title = "d" // title is selection argument (see @selection directive in schema)

                    // id is primary key
                    // title is default
                    genre()
                    // countryId is required
                    actors {
                        limit = -1 // limit is selection argument (see @selection directive in schema)

                        // id is primary key
                        // firstName is default
                        // lastName is default
                        // birthday is required (see @required directive in schema)
                        gender()
                        // countryId is primary key
                        country {
                            // id is primary key
                            // name is default
                        }
                    }
                }
                actors {
                    firstName = "d" // firstName is selection argument (see @selection directive in schema)

                    // birthdayFrom is selection argument (see @selection directive in schema)
                    birthdayFrom = LocalDate.of(1970, 1, 1)

                    // id is primary key
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
                    // countryId is primary key
                    films {
                        limit = -1 // limit is selection argument (see @selection directive in schema)

                        // id is primary key
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
                    "${it.firstName} ${it.lastName} (${it.gender.name.lowercase()}) from ${it.country.name}"
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

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**              Let try to select interfaces                    **")
        println("******************************************************************")
        println()

        // query($arg0: String!) { taggable(tag: $arg0) { id tags { value } __typename ... on Film { title genre countryId } ... on Actor { firstName lastName birthday gender countryId country { id name } } } }
        // {arg0=best}
        context.query {
            taggable("best") {
                // id is primary key
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
                    // countryId is primary key
                    country {
                        // id is primary key
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
                        "Actor[$tags]: ${cur.firstName} ${cur.lastName} (${cur.gender.name.lowercase()}) " +
                                "from ${cur.country.name}"
                    )
                }

                else -> error("Invalid algorithm")
            }
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**                Let try to select unions                      **")
        println("******************************************************************")
        println()

        // query($arg0: ID!) { country(id: $arg0) { id native { __typename ... on Film { id title genre countryId } ... on Actor { id firstName lastName birthday gender countryId country { id name } } } } }
        // {arg0=17}
        context.query {
            country(17) {
                __minimize() // switch off defaults to minimize query
                // id is primary key
                native {
                    // __typename generated by Kobby
                    __onFilm {
                        // id is primary key
                        // title is default
                        genre()
                        // countryId is required
                    }
                    __onActor {
                        // id is primary key
                        // firstName is default
                        // lastName is default
                        // birthday is required
                        gender()
                        // countryId is primary key
                        country {
                            // id is primary key
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
                        "Actor: ${cur.firstName} ${cur.lastName} (${cur.gender.name.lowercase()}) " +
                                "from ${cur.country.name}"
                    )
                }

                else -> error("Invalid algorithm")
            }
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**                Let try our customized API                    **")
        println("******************************************************************")
        println()

        println(">> Fetch country by id")
        val country = context.fetchCountry(7)
        println("Country: id=${country.id} name='${country.name}'")

        println()
        println(">> Find all country films")
        val films = country.findFilms {
            limit = -1
            genre()
        }

        films.forEach {
            println("Film: id=${it.id}, title='${it.title}' genre=${it.genre}")
        }
    }


    //******************************************************************************************************************
    //                                           Composite Adapter
    //******************************************************************************************************************
    suspend fun compositeKtorAdapterExample() {
        val client = HttpClient(CIO) {
            install(WebSockets)
        }

        val context = cinemaContextOf(
            CinemaCompositeKtorAdapter(
                client,
                "http://localhost:8080/graphql",
                "ws://localhost:8080/subscriptions",
                requestHeaders = { mapOf("Authorization" to "Basic YWRtaW46YWRtaW4=") },
                subscriptionReceiveTimeoutMillis = 23000
            ) {
                println(">> ${it.query}")
                println(">> ${it.variables}")
            }
        )

        println()
        println()
        println("##################################################################")
        println("##            Composite Ktor Adapter Example                    ##")
        println("##################################################################")
        println()
        println("******************************************************************")
        println("**         Let try 'countryCreated' subscription                **")
        println("******************************************************************")
        println()

        context.subscription {
            countryCreated {
                // id is primary key (see @primaryKey directive in schema)
                // name is default (see @default directive in schema)
            }
        }.subscribe {
            println()
            context.createCountry("First")
            context.createCountry("Second")
            context.createCountry("Third")
            println()

            for (i in 1..3) {
                receive().countryCreated.also {
                    println("<< Country created: id=${it.id} name=${it.name}")
                }
            }
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("** Let try 'filmCreated' subscription with filtering by country **")
        println("******************************************************************")
        println()

        context.subscription {
            filmCreated(countryId = 1) {
                country()
            }
        }.subscribe {
            println()
            context.createFilm(1, FilmInput("First"))
            context.createFilm(0, FilmInput("Second"))
            context.createFilm(1, FilmInput("Third"))
            println()

            for (i in 1..2) {
                receive().filmCreated.also {
                    println("<< Film created: id=${it.id} name=${it.title} country=${it.country.name}")
                }
            }
        }

        //**************************************************************************************************************

        println()
        println("******************************************************************")
        println("**   Let try 'actorCreated' subscription with customized API    **")
        println("******************************************************************")
        println()

        val brazil: Country = context.fetchCountry(4)
        val spain: Country = context.fetchCountry(15)
        spain.onActorCreated { gender(); country() }.subscribe {
            println()
            spain.createActor(ActorInput {
                firstName = "First"
                birthday = LocalDate.now()
                gender = Gender.FEMALE
            })
            brazil.createActor(ActorInput {
                firstName = "Second"
                birthday = LocalDate.now()
                gender = Gender.FEMALE
            })
            spain.createActor(ActorInput {
                firstName = "Third"
                birthday = LocalDate.now()
                gender = Gender.MALE
            })
            println()

            for (i in 1..2) {
                val createdActor: Actor = receive()
                println(
                    "<< Actor created: " +
                            "id=${createdActor.id} " +
                            "name=${createdActor.firstName} " +
                            "gender=${createdActor.gender} " +
                            "country=${createdActor.country.name}"
                )
            }
        }

        println()
        println("##################################################################")
    }
}