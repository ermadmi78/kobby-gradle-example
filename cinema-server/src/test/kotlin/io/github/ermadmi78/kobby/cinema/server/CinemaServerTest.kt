package io.github.ermadmi78.kobby.cinema.server

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaContext
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.spring.SpringListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

/**
 * Install Kotest plugin to run tests from IntelliJ IDEA
 * https://plugins.jetbrains.com/plugin/14080-kotest
 */
@SpringBootTest(classes = [Application::class])
class CinemaServerTest : AnnotationSpec() {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    lateinit var cinemaContext: CinemaContext


    override fun listeners() = listOf(SpringListener)

    @BeforeAll
    fun setUp() {
        cinemaContext = cinemaContextOf(
            CinemaTestAdapter(
                WebTestClient.bindToApplicationContext(applicationContext).build(),
                jacksonObjectMapper()
                    .registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                    .registerModule(JavaTimeModule())
                    // Force Jackson to serialize dates as String
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            )
        )
    }

    @Test
    fun country() = runBlocking {
        val country = cinemaContext.query {
            country(0)
        }.country!!

        country.id shouldBe 0
        country.name shouldBe "Argentina"

        shouldThrow<IllegalStateException> {
            country.fields
        }.message shouldBe "Property [fields] is not available - add [fields] projection to switch on it"
    }

    @Test
    fun createCountryWithFilmAndActors() = runBlocking {
        val country = cinemaContext.mutation {
            createCountry("USSR")
        }.createCountry

        country.name shouldBe "USSR"

        val film = cinemaContext.mutation {
            createFilm(country.id, FilmInput("Hedgehog in the fog")) {
                // tags is selection argument - see @selection directive
                tags = TagInput {
                    value = "cool"
                }

                genre()
                country()
                tags {
                    value()
                }
            }
        }.createFilm

        film.title shouldBe "Hedgehog in the fog"
        film.tags.also {
            it.size shouldBe 1
            it[0].value shouldBe "cool"
        }
        film.genre shouldBe Genre.DRAMA
        film.countryId shouldBe country.id
        film.country.id shouldBe country.id
        film.country.name shouldBe "USSR"

        val actor = cinemaContext.mutation {
            createActor(country.id, ActorInput {
                firstName = "Hedgehog"
                birthday = LocalDate.of(1975, 3, 15)
                gender = Gender.MALE
            }) {
                gender()
                country()
                tags {
                    value()
                }
            }
        }.createActor

        actor.firstName shouldBe "Hedgehog"
        actor.lastName shouldBe null
        actor.birthday shouldBe LocalDate.of(1975, 3, 15)
        actor.gender shouldBe Gender.MALE
        actor.countryId shouldBe country.id
        actor.country.id shouldBe country.id
        actor.country.name shouldBe "USSR"

        cinemaContext.mutation {
            associate(film.id, actor.id)
        }.associate shouldBe true

        cinemaContext.mutation {
            associate(film.id, actor.id)
        }.associate shouldBe false

        cinemaContext.mutation {
            tagFilm(film.id, "cool")
        }.tagFilm shouldBe false

        cinemaContext.mutation {
            tagActor(actor.id, "cool")
        }.tagActor shouldBe true

        val ussr = cinemaContext.query {
            country(country.id) {
                films {
                    limit = -1
                    genre()
                    country()
                    tags {
                        value()
                    }

                    // Actors of film
                    actors {
                        limit = -1
                        gender()
                        country()
                        tags {
                            value()
                        }
                    }
                }
            }
        }.country!!

        ussr.name shouldBe "USSR"
        ussr.films.also { ussrFilms ->
            ussrFilms.size shouldBe 1

            ussrFilms[0].title shouldBe "Hedgehog in the fog"
            ussrFilms[0].genre shouldBe Genre.DRAMA
            ussrFilms[0].countryId shouldBe country.id
            ussrFilms[0].country.id shouldBe country.id
            ussrFilms[0].country.name shouldBe "USSR"
            ussrFilms[0].tags.also {
                it.size shouldBe 1
                it[0].value shouldBe "cool"
            }
            ussrFilms[0].actors.also { filmActors ->
                filmActors.size shouldBe 1

                filmActors[0].firstName shouldBe "Hedgehog"
                filmActors[0].lastName shouldBe null
                filmActors[0].birthday shouldBe LocalDate.of(1975, 3, 15)
                filmActors[0].gender shouldBe Gender.MALE
                filmActors[0].countryId shouldBe country.id
                filmActors[0].country.id shouldBe country.id
                filmActors[0].country.name shouldBe "USSR"
                filmActors[0].tags.also {
                    it.size shouldBe 1
                    it[0].value shouldBe "cool"
                }
            }
        }
    }
}