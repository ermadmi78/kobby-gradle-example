package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaFilmResolver
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
class FilmResolver(private val dslContext: DSLContext) : CinemaFilmResolver {
    companion object {
        private val ALL_FIELDS = setOf("id", "title", "genre")
    }

    override suspend fun fields(
        film: FilmDto,
        keys: List<String>?
    ): JsonObject = buildJsonObject {
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> put(it, film.id)
                "title" -> put(it, film.title)
                "genre" -> put(it, film.genre?.name)
            }
        }
    }

    override suspend fun country(film: FilmDto): CountryDto = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(film.countryId))
            .fetchAny { it.toDto() }!!
    }

    override suspend fun actors(
        film: FilmDto,
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.select(ACTOR.asterisk())
            .from(ACTOR)
            .join(FILM_ACTOR)
            .onKey()
            .where(FILM_ACTOR.FILM_ID.eq(film.id))
            .and(trueCondition().andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
            .limit(offset.prepare(), limit.prepare())
            .fetch {
                it.into(ACTOR).toDto()
            }
    }
}