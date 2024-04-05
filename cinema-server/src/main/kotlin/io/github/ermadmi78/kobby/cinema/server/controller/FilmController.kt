package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Controller
@SchemaMapping(typeName = "Film")
class FilmController(private val dslContext: DSLContext) {
    companion object {
        private val ALL_FIELDS = setOf("id", "title", "genre")
    }

    @SchemaMapping
    suspend fun fields(
        film: FilmDto,
        @Argument keys: List<String>?
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> result[it] = film.id
                "title" -> result[it] = film.title
                "genre" -> result[it] = film.genre
            }
        }

        return result
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun country(film: FilmDto): CountryDto = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(film.countryId))
            .fetchAny { it.toDto() }!!
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actors(
        film: FilmDto,
        @Argument firstName: String?,
        @Argument lastName: String?,
        @Argument birthdayFrom: LocalDate?,
        @Argument birthdayTo: LocalDate?,
        @Argument gender: Gender?,
        @Argument limit: Int,
        @Argument offset: Int
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