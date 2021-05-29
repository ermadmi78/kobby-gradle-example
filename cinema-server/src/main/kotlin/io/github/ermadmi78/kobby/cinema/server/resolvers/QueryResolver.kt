package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaQueryResolver
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class QueryResolver(private val dslContext: DSLContext) : CinemaQueryResolver {
    /**
     * Coroutine based resolver authorization example
     */
    override suspend fun country(id: Long): CountryDto? = hasAnyRole("USER", "ADMIN") {
        println("Query country by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    override suspend fun countries(
        name: String?,
        limit: Int,
        offset: Int
    ): List<CountryDto> = hasAnyRole("USER", "ADMIN") {
        println("Query countries by user [${authentication.name}] in thread [${Thread.currentThread().name}]")

        var condition: Condition = trueCondition()

        if (!name.isNullOrBlank()) {
            condition = condition.and(COUNTRY.NAME.containsIgnoreCase(name.trim()))
        }

        dslContext.selectFrom(COUNTRY)
            .where(condition)
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    override suspend fun film(id: Long): FilmDto? = hasAnyRole("USER", "ADMIN") {
        println("Query film by user [${authentication.name}] in thread [${Thread.currentThread().name}]")

        dslContext.selectFrom(FILM)
            .where(FILM.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    override suspend fun films(
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

    override suspend fun actor(id: Long): ActorDto? = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(ACTOR.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    override suspend fun actors(
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

    override suspend fun taggable(tag: String): List<TaggableDto> = hasAnyRole("USER", "ADMIN") {
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