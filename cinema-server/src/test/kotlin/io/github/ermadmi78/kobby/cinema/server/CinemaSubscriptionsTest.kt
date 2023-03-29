package io.github.ermadmi78.kobby.cinema.server

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.adapter.ktor.CinemaCompositeKtorAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorInput
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmInput
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender.FEMALE
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender.MALE
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Genre.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.createActor
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.createFilm
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.onActorCreated
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.onFilmCreated
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.time.LocalDate


/**
 * Install Kotest plugin to run tests from IntelliJ IDEA
 * https://plugins.jetbrains.com/plugin/14080-kotest
 *
 * Created on 18.06.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CinemaSubscriptionsTest : AnnotationSpec() {
    @LocalServerPort
    var port: Int? = null

    lateinit var context: CinemaContext

    override fun extensions() = listOf(SpringExtension)

    @BeforeAll
    fun setUp() {
        val client = HttpClient(CIO) {
            install(WebSockets)
        }

        context = cinemaContextOf(
            CinemaCompositeKtorAdapter(
                client,
                "http://localhost:${port!!}/graphql",
                "ws://localhost:${port!!}/subscriptions",
                requestHeaders = { mapOf("Authorization" to "Basic YWRtaW46YWRtaW4=") }
            )
        )
    }

    @Test
    fun subscriptionsByMeansOfGeneratedAPI() = runBlocking {
        // Created countries subscription
        context.subscription {
            countryCreated()
        }.subscribe {
            // Create countries
            context.mutation {
                createCountry("First")
            }
            context.mutation {
                createCountry("Second")
            }
            context.mutation {
                createCountry("Third")
            }

            // Listen created countries
            receive().countryCreated.name shouldBe "First"
            receive().countryCreated.name shouldBe "Second"
            receive().countryCreated.name shouldBe "Third"
        }

        // Created films subscription
        context.subscription {
            filmCreated(1) {
                genre()
                country()
            }
        }.subscribe {
            // Create films
            context.mutation {
                createFilm(1, FilmInput("First", COMEDY))
            }
            context.mutation {
                createFilm(5, FilmInput("Second", THRILLER))
            }
            context.mutation {
                createFilm(1, FilmInput("Third", HORROR))
            }

            // Listen created films
            receive().filmCreated.also { first ->
                first.title shouldBe "First"
                first.genre shouldBe COMEDY
                first.country.name shouldBe "Australia"
            }
            receive().filmCreated.also { third ->
                third.title shouldBe "Third"
                third.genre shouldBe HORROR
                third.country.name shouldBe "Australia"
            }
        }

        // Created actors subscription
        context.subscription {
            actorCreated(1) {
                gender()
                country()
            }
        }.subscribe {
            // Create actors
            val now = LocalDate.now()
            context.mutation {
                createActor(1, ActorInput {
                    firstName = "First"
                    lastName = "Actress"
                    birthday = now
                    gender = FEMALE
                })
            }
            context.mutation {
                createActor(5, ActorInput {
                    firstName = "Second"
                    lastName = "Actress"
                    birthday = now
                    gender = FEMALE
                })
            }
            context.mutation {
                createActor(1, ActorInput {
                    firstName = "Third"
                    lastName = "Actor"
                    birthday = now
                    gender = MALE
                })
            }

            // Listen created actors
            receive().actorCreated.also { first ->
                first.firstName shouldBe "First"
                first.lastName shouldBe "Actress"
                first.birthday shouldBe now
                first.gender shouldBe FEMALE
                first.country.name shouldBe "Australia"
            }
            receive().actorCreated.also { third ->
                third.firstName shouldBe "Third"
                third.lastName shouldBe "Actor"
                third.birthday shouldBe now
                third.gender shouldBe MALE
                third.country.name shouldBe "Australia"
            }
        }
    }

    @Test
    fun subscriptionsByMeansOfCustomizedAPI() = runBlocking {
        // Created countries subscription
        context.onCountryCreated().subscribe {
            // Create countries
            context.createCountry("First")
            context.createCountry("Second")
            context.createCountry("Third")

            // Listen created countries
            receive().name shouldBe "First"
            receive().name shouldBe "Second"
            receive().name shouldBe "Third"
        }

        // Prepare countries
        val australia = context.fetchCountry(1)
        val canada = context.fetchCountry(5)

        // Created films subscription
        australia.onFilmCreated { genre(); country() }.subscribe {
            // Create films
            australia.createFilm(FilmInput("First", COMEDY))
            canada.createFilm(FilmInput("Second", THRILLER))
            australia.createFilm(FilmInput("Third", HORROR))

            // Listen created films
            receive().also { first ->
                first.title shouldBe "First"
                first.genre shouldBe COMEDY
                first.country.name shouldBe "Australia"
            }
            receive().also { third ->
                third.title shouldBe "Third"
                third.genre shouldBe HORROR
                third.country.name shouldBe "Australia"
            }
        }

        // Created actors subscription
        australia.onActorCreated { gender(); country() }.subscribe {
            // Create actors
            val now = LocalDate.now()
            australia.createActor(ActorInput {
                firstName = "First"
                lastName = "Actress"
                birthday = now
                gender = FEMALE
            })
            canada.createActor(ActorInput {
                firstName = "Second"
                lastName = "Actress"
                birthday = now
                gender = FEMALE
            })
            australia.createActor(ActorInput {
                firstName = "Third"
                lastName = "Actor"
                birthday = now
                gender = MALE
            })

            // Listen created actors
            receive().also { first ->
                first.firstName shouldBe "First"
                first.lastName shouldBe "Actress"
                first.birthday shouldBe now
                first.gender shouldBe FEMALE
                first.country.name shouldBe "Australia"
            }
            receive().also { third ->
                third.firstName shouldBe "Third"
                third.lastName shouldBe "Actor"
                third.birthday shouldBe now
                third.gender shouldBe MALE
                third.country.name shouldBe "Australia"
            }
        }
    }
}