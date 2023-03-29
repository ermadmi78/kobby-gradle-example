package io.github.ermadmi78.kobby.cinema.server

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaContext
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.adapter.ktor.CinemaSimpleKtorAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaJson
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.createCountry
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.time.LocalDate

/**
 * Install Kotest plugin to run tests from IntelliJ IDEA
 * https://plugins.jetbrains.com/plugin/14080-kotest
 */
@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CinemaServerTest : AnnotationSpec() {
    @LocalServerPort
    var port: Int? = null

    lateinit var context: CinemaContext

    override fun extensions() = listOf(SpringExtension)

    @BeforeAll
    fun setUp() {
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

        context = cinemaContextOf(
            CinemaSimpleKtorAdapter(client, "http://localhost:${port!!}/graphql")
        )
    }

    @Test
    fun createCountryWithFilmAndActorsByMeansOfGeneratedAPI() = runBlocking {
        val country = context.mutation {
            createCountry("USSR")
        }.createCountry

        country.name shouldBe "USSR"

        val film = context.mutation {
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
        film.toString() shouldBe "Film(id=${film.id}, tags=[Tag(value=cool)], title=Hedgehog in the fog, genre=DRAMA, countryId=${film.countryId}, country=Country(id=${film.country.id}, name=USSR))"

        var actor = context.mutation {
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
        actor.tags.isEmpty() shouldBe true
        actor.countryId shouldBe country.id
        actor.country.id shouldBe country.id
        actor.country.name shouldBe "USSR"
        actor.toString() shouldBe "Actor(id=${actor.id}, tags=[], firstName=Hedgehog, lastName=null, birthday=1975-03-15, gender=MALE, countryId=${actor.countryId}, country=Country(id=${actor.country.id}, name=USSR))"

        actor = context.mutation {
            updateBirthday(actor.id, LocalDate.of(1976, 4, 16)) {
                gender()
                country()
                tags {
                    value()
                }
            }
        }.updateBirthday!!

        actor.firstName shouldBe "Hedgehog"
        actor.lastName shouldBe null
        actor.birthday shouldBe LocalDate.of(1976, 4, 16)
        actor.gender shouldBe Gender.MALE
        actor.tags.isEmpty() shouldBe true
        actor.countryId shouldBe country.id
        actor.country.id shouldBe country.id
        actor.country.name shouldBe "USSR"
        actor.toString() shouldBe "Actor(id=${actor.id}, tags=[], firstName=Hedgehog, lastName=null, birthday=1976-04-16, gender=MALE, countryId=${actor.countryId}, country=Country(id=${actor.country.id}, name=USSR))"

        context.mutation {
            associate(film.id, actor.id)
        }.associate shouldBe true

        context.mutation {
            associate(film.id, actor.id)
        }.associate shouldBe false

        context.mutation {
            tagFilm(film.id, "cool")
        }.tagFilm shouldBe false

        context.mutation {
            tagActor(actor.id, "cool")
        }.tagActor shouldBe true

        val ussr = context.query {
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
                filmActors[0].birthday shouldBe LocalDate.of(1976, 4, 16)
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
        ussr.toString() shouldBe "Country(id=${country.id}, name=USSR, films=[Film(id=${film.id}, tags=[Tag(value=cool)], title=Hedgehog in the fog, genre=DRAMA, countryId=${country.id}, country=Country(id=${country.id}, name=USSR), actors=[Actor(id=${actor.id}, tags=[Tag(value=cool)], firstName=Hedgehog, lastName=null, birthday=1976-04-16, gender=MALE, countryId=${country.id}, country=Country(id=${country.id}, name=USSR))])])"
    }

    @Test
    fun createCountryWithFilmAndActorsByMeansOfCustomizedAPI() = runBlocking {
        val country = context.createCountry("USSR")

        country.name shouldBe "USSR"

        val film = country.createFilm(FilmInput("Hedgehog in the fog")) {
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

        film.title shouldBe "Hedgehog in the fog"
        film.tags.also {
            it.size shouldBe 1
            it[0].value shouldBe "cool"
        }
        film.genre shouldBe Genre.DRAMA
        film.countryId shouldBe country.id
        film.country.id shouldBe country.id
        film.country.name shouldBe "USSR"
        film.toString() shouldBe "Film(id=${film.id}, tags=[Tag(value=cool)], title=Hedgehog in the fog, genre=DRAMA, countryId=${film.countryId}, country=Country(id=${film.country.id}, name=USSR))"

        var actor = country.createActor(ActorInput {
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

        actor.firstName shouldBe "Hedgehog"
        actor.lastName shouldBe null
        actor.birthday shouldBe LocalDate.of(1975, 3, 15)
        actor.gender shouldBe Gender.MALE
        actor.tags.isEmpty() shouldBe true
        actor.countryId shouldBe country.id
        actor.country.id shouldBe country.id
        actor.country.name shouldBe "USSR"
        actor.toString() shouldBe "Actor(id=${actor.id}, tags=[], firstName=Hedgehog, lastName=null, birthday=1975-03-15, gender=MALE, countryId=${actor.countryId}, country=Country(id=${actor.country.id}, name=USSR))"

        actor = actor.updateBirthday(LocalDate.of(1976, 4, 16))

        actor.firstName shouldBe "Hedgehog"
        actor.lastName shouldBe null
        actor.birthday shouldBe LocalDate.of(1976, 4, 16)
        actor.gender shouldBe Gender.MALE
        actor.tags.isEmpty() shouldBe true
        actor.countryId shouldBe country.id
        actor.country.id shouldBe country.id
        actor.country.name shouldBe "USSR"
        actor.toString() shouldBe "Actor(id=${actor.id}, tags=[], firstName=Hedgehog, lastName=null, birthday=1976-04-16, gender=MALE, countryId=${actor.countryId}, country=Country(id=${actor.country.id}, name=USSR))"

        film.addActor(actor.id) shouldBe true
        actor.addFilm(film.id) shouldBe false

        film.tag("cool") shouldBe false
        actor.tag("cool") shouldBe true

        val ussr = country.refresh {
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
                filmActors[0].birthday shouldBe LocalDate.of(1976, 4, 16)
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
        ussr.toString() shouldBe "Country(id=${country.id}, name=USSR, films=[Film(id=${film.id}, tags=[Tag(value=cool)], title=Hedgehog in the fog, genre=DRAMA, countryId=${country.id}, country=Country(id=${country.id}, name=USSR), actors=[Actor(id=${actor.id}, tags=[Tag(value=cool)], firstName=Hedgehog, lastName=null, birthday=1976-04-16, gender=MALE, countryId=${country.id}, country=Country(id=${country.id}, name=USSR))])])"
    }

    @Test
    fun simpleQuery() = runBlocking {
        val country = context.query {
            country(0)
        }.country!!

        country.id shouldBe 0
        country.name shouldBe "Argentina"

        shouldThrow<IllegalStateException> {
            country.fields
        }.message shouldBe "Property [fields] is not available - add [fields] projection to switch on it"

        val films = context.query {
            films {
                genre = Genre.COMEDY
                offset = 2
                limit = 2
                genre()
                fields {
                    keys = listOf("title", "genre")
                }
            }
        }.films

        films.size shouldBe 2
        films[0].apply {
            id shouldBe 4
            title shouldBe "House"
            genre shouldBe Genre.COMEDY
            countryId shouldBe 17
            shouldThrow<IllegalStateException> {
                this.country
            }.message shouldBe "Property [country] is not available - add [country] projection to switch on it"
            fields shouldContainExactly buildJsonObject {
                put("title", "House")
                put("genre", "COMEDY")
            }
        }
        films[1].apply {
            id shouldBe 5
            title shouldBe "Peter's Friends"
            genre shouldBe Genre.COMEDY
            countryId shouldBe 17
            fields shouldContainExactly buildJsonObject {
                put("title", "Peter's Friends")
                put("genre", "COMEDY")
            }
        }

        val actors = context.query {
            actors {
                gender = Gender.FEMALE
                birthdayFrom = LocalDate.of(1967, 1, 1)

                gender()
            }
        }.actors
        actors.size shouldBe 2
        actors[0].apply {
            id shouldBe 0
            firstName shouldBe "Audrey"
            lastName shouldBe "Tautou"
            gender shouldBe Gender.FEMALE
            birthday shouldBe LocalDate.of(1976, 8, 9)
        }
        actors[1].apply {
            id shouldBe 10
            firstName shouldBe "Julia"
            lastName shouldBe "Roberts"
            gender shouldBe Gender.FEMALE
            birthday shouldBe LocalDate.of(1967, 10, 28)
        }
    }

    @Test
    fun complexQuery() = runBlocking {
        val usa = context.query {
            country(18) {
                films {
                    title = "d"
                    genre()
                    actors {
                        gender()
                        country()
                    }
                }
                actors {
                    limit = 2
                    gender()
                    films {
                        genre = Genre.THRILLER
                        genre()
                        country()
                    }
                }
            }
        }.country!!
        usa.id shouldBe 18
        usa.name shouldBe "USA"

        val usaFilms = usa.films
        usaFilms.size shouldBe 1
        usaFilms[0].also { film ->
            film.id shouldBe 11
            film.title shouldBe "From Dusk Till Dawn"
            film.genre shouldBe Genre.THRILLER
            film.countryId shouldBe 18

            film.actors.size shouldBe 2
            film.actors[0].apply {
                id shouldBe 11
                firstName shouldBe "George"
                lastName shouldBe "Clooney"
                birthday shouldBe LocalDate.of(1967, 10, 28)
                gender shouldBe Gender.MALE
                countryId shouldBe 18
                country.id shouldBe 18
                country.name shouldBe "USA"
            }
            film.actors[1].apply {
                id shouldBe 15
                firstName shouldBe "Salma"
                lastName shouldBe "Hayek"
                birthday shouldBe LocalDate.of(1966, 9, 2)
                gender shouldBe Gender.FEMALE
                countryId shouldBe 18
                country.id shouldBe 18
                country.name shouldBe "USA"
            }
        }

        val usaActors = usa.actors
        usaActors.size shouldBe 2
        usaActors[0].also { actor ->
            actor.id shouldBe 9
            actor.firstName shouldBe "Keanu"
            actor.lastName shouldBe "Reeves"
            actor.birthday shouldBe LocalDate.of(1964, 9, 2)
            actor.gender shouldBe Gender.MALE
            actor.countryId shouldBe 18

            actor.films.size shouldBe 1
            actor.films[0].apply {
                id shouldBe 6
                title shouldBe "Street Kings"
                genre shouldBe Genre.THRILLER
                countryId shouldBe 17
                country.id shouldBe 17
                country.name shouldBe "United Kingdom"
            }
        }
        usaActors[1].also { actor ->
            actor.id shouldBe 10
            actor.firstName shouldBe "Julia"
            actor.lastName shouldBe "Roberts"
            actor.birthday shouldBe LocalDate.of(1967, 10, 28)
            actor.gender shouldBe Gender.FEMALE
            actor.countryId shouldBe 18

            actor.films.size shouldBe 1
            actor.films[0].apply {
                id shouldBe 8
                title shouldBe "Ocean's Eleven"
                genre shouldBe Genre.THRILLER
                countryId shouldBe 18
                country.id shouldBe 18
                country.name shouldBe "USA"
            }
        }
    }

    @Test
    fun interfaceQuery() = runBlocking {
        val list = context.query {
            taggable("julia") {
                tags {
                    value()
                }
                __onFilm {
                    genre()
                }
                __onActor {
                    gender()
                }
            }
        }.taggable

        list.size shouldBe 4
        list[0].also {
            it.id shouldBe 8
            it.tags.size shouldBe 3
            it.tags[0].value shouldBe "best"
            it.tags[1].value shouldBe "julia"
            it.tags[2].value shouldBe "clooney"
            (it as Film).apply {
                title shouldBe "Ocean's Eleven"
                genre shouldBe Genre.THRILLER
                countryId shouldBe 18
            }
        }
        list[1].also {
            it.id shouldBe 9
            it.tags.size shouldBe 1
            it.tags[0].value shouldBe "julia"
            (it as Film).apply {
                title shouldBe "Stepmom"
                genre shouldBe Genre.DRAMA
                countryId shouldBe 18
            }
        }
        list[2].also {
            it.id shouldBe 10
            it.tags.size shouldBe 1
            it.tags[0].value shouldBe "julia"
            (it as Film).apply {
                title shouldBe "Pretty Woman"
                genre shouldBe Genre.COMEDY
                countryId shouldBe 18
            }
        }
        list[3].also {
            it.id shouldBe 10
            it.tags.size shouldBe 2
            it.tags[0].value shouldBe "best"
            it.tags[1].value shouldBe "julia"
            (it as Actor).apply {
                firstName shouldBe "Julia"
                lastName shouldBe "Roberts"
                birthday shouldBe LocalDate.of(1967, 10, 28)
                gender shouldBe Gender.FEMALE
                countryId shouldBe 18
            }
        }
    }

    @Test
    fun unionQuery() = runBlocking {
        val list = context.query {
            country(17) {
                __minimize()
                native {
                    __onFilm {
                        genre()
                    }
                    __onActor {
                        gender()
                    }
                }
            }
        }.country!!.native

        list.size shouldBe 6
        (list[0] as Film).apply {
            id shouldBe 4
            title shouldBe "House"
            genre shouldBe Genre.COMEDY
            countryId shouldBe 17
        }
        (list[1] as Film).apply {
            id shouldBe 5
            title shouldBe "Peter's Friends"
            genre shouldBe Genre.COMEDY
            countryId shouldBe 17
        }
        (list[2] as Film).apply {
            id shouldBe 6
            title shouldBe "Street Kings"
            genre shouldBe Genre.THRILLER
            countryId shouldBe 17
        }
        (list[3] as Film).apply {
            id shouldBe 7
            title shouldBe "Mr. Pip"
            genre shouldBe Genre.DRAMA
            countryId shouldBe 17
        }
        (list[4] as Actor).apply {
            id shouldBe 7
            firstName shouldBe "Hugh"
            lastName shouldBe "Laurie"
            birthday shouldBe LocalDate.of(1959, 6, 11)
            gender shouldBe Gender.MALE
            countryId shouldBe 17
        }
        (list[5] as Actor).apply {
            id shouldBe 8
            firstName shouldBe "Stephen"
            lastName shouldBe "Fry"
            birthday shouldBe LocalDate.of(1957, 8, 24)
            gender shouldBe Gender.MALE
            countryId shouldBe 17
        }
    }

    @Test
    fun unqualifiedInterfaceQuery() = runBlocking {
        val list = context.query {
            taggable("julia") {
                tags {
                    value()
                }
            }
        }.taggable

        list.size shouldBe 4
        list[0].also {
            it.id shouldBe 8
            it.tags.size shouldBe 3
            it.tags[0].value shouldBe "best"
            it.tags[1].value shouldBe "julia"
            it.tags[2].value shouldBe "clooney"
            (it as Film).apply {
                title shouldBe "Ocean's Eleven"
                countryId shouldBe 18
                shouldThrow<IllegalStateException> {
                    genre
                }.message shouldBe "Property [genre] is not available - add [genre] projection to switch on it"
            }
        }
        list[3].also {
            it.id shouldBe 10
            it.tags.size shouldBe 2
            it.tags[0].value shouldBe "best"
            it.tags[1].value shouldBe "julia"
            (it as Actor).apply {
                firstName shouldBe "Julia"
                lastName shouldBe "Roberts"
                birthday shouldBe LocalDate.of(1967, 10, 28)
                countryId shouldBe 18
                shouldThrow<IllegalStateException> {
                    gender
                }.message shouldBe "Property [gender] is not available - add [gender] projection to switch on it"
            }
        }
    }

    @Test
    fun unqualifiedUnionQuery() = runBlocking {
        val list = context.query {
            country(17) {
                native()
            }
        }.country!!.native

        list.size shouldBe 6
        (list[0] as Film).apply {
            id shouldBe 4
            title shouldBe "House"
            countryId shouldBe 17
            shouldThrow<IllegalStateException> {
                genre
            }.message shouldBe "Property [genre] is not available - add [genre] projection to switch on it"
        }
        (list[5] as Actor).apply {
            id shouldBe 8
            firstName shouldBe "Stephen"
            lastName shouldBe "Fry"
            birthday shouldBe LocalDate.of(1957, 8, 24)
            countryId shouldBe 17
            shouldThrow<IllegalStateException> {
                gender
            }.message shouldBe "Property [gender] is not available - add [gender] projection to switch on it"
        }
    }
}