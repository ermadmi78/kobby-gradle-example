package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import io.github.ermadmi78.kobby.cinema.server.security.getAuthentication
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
@ExperimentalCoroutinesApi
class QueryResolver(private val resolverDispatcher: CoroutineDispatcher) : GraphQLQueryResolver {
    @Autowired
    private lateinit var dslContext: DSLContext

    /**
     * Coroutine based resolver authorization example
     */
    suspend fun country(id: Long): CountryDto? = hasAnyRole("USER", "ADMIN") {
        println("Query country by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    /**
     * Flux based resolver authorization example
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun countries(
        name: String?,
        limit: Int,
        offset: Int
    ): Flux<CountryDto> = flux(resolverDispatcher) {
        val authentication = getAuthentication()!!
        println("Query countries by user [${authentication.name}] in thread [${Thread.currentThread().name}]")

        var condition: Condition = trueCondition()

        if (!name.isNullOrBlank()) {
            condition = condition.and(COUNTRY.NAME.containsIgnoreCase(name.trim()))
        }

        dslContext.selectFrom(COUNTRY)
            .where(condition)
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
            .forEach {
                send(it)
            }
    }

    /**
     * Mono based resolver authorization example
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun film(id: Long): Mono<FilmDto?> = mono(resolverDispatcher) {
        val authentication = getAuthentication()!!
        println("Query film by user [${authentication.name}] in thread [${Thread.currentThread().name}]")

        dslContext.selectFrom(FILM)
            .where(FILM.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    suspend fun films(
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(FILM)
            .where(trueCondition().andFilms(title, genre))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    suspend fun actor(id: Long): ActorDto? = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(ACTOR.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    suspend fun actors(
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(trueCondition().andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    suspend fun taggable(tag: String): List<TaggableDto> = hasAnyRole("USER", "ADMIN") {
        val result = mutableListOf<TaggableDto>()

        dslContext.selectFrom(FILM).where(filmTagsContains(tag)).forEach {
            result.add(it.toDto())
        }

        dslContext.selectFrom(ACTOR).where(actorTagsContains(tag)).forEach {
            result.add(it.toDto())
        }

        result
    }
}