package io.github.ermadmi78.kobby.cinema.server

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaContext
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.spring.SpringListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

/**
 * Install Kotest plugin to start tests from IntelliJ IDEA
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
        TimeZone.setDefault(TimeZone.getTimeZone("Z"))
        val mapper = jacksonObjectMapper()
        mapper.registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        mapper.registerModule(JavaTimeModule())
        cinemaContext = cinemaContextOf(
            CinemaTestAdapter(
                WebTestClient.bindToApplicationContext(applicationContext).build(),
                mapper
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
    fun actorFirst() = runBlocking {
        val actor = cinemaContext.query {
            actor(0)
        }.actor!!

        actor.id shouldBe 0
        actor.firstName shouldBe "Audrey"

        shouldThrow<IllegalStateException> {
            actor.fields
        }.message shouldBe "Property [fields] is not available - add [fields] projection to switch on it"
    }


    @Test
    fun actorSecond() = runBlocking {
        val actor = cinemaContext.query {
            actor(2)
        }.actor!!

        actor.id shouldNotBe 0
        actor.firstName shouldBe "Jamel"

        shouldThrow<IllegalStateException> {
            actor.fields
        }.message shouldBe "Property [fields] is not available - add [fields] projection to switch on it"
    }
}