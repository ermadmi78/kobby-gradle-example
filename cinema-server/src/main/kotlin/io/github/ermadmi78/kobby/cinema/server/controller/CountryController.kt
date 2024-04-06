package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.ACTOR
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.FILM
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jooq.DSLContext
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
@SchemaMapping(typeName = "Country")
class CountryController(private val dslContext: DSLContext) {
    companion object {
        private val ALL_FIELDS = setOf("id", "name")
    }

    @SchemaMapping
    suspend fun fields(
        country: CountryDto,
        @Argument keys: List<String>?
    ): JsonObject = buildJsonObject {
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> put(it, country.id)
                "name" -> put(it, country.name)
            }
        }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun film(
        country: CountryDto,
        @Argument id: Long
    ): FilmDto? = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(FILM)
            .where(FILM.COUNTRY_ID.eq(country.id).and(FILM.ID.eq(id)))
            .fetchAny { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun films(
        country: CountryDto,
        @Argument title: String?,
        @Argument genre: Genre?,
        @Argument limit: Int,
        @Argument offset: Int
    ): List<FilmDto> = hasAnyRole("USER", "ADMIN") {
        println("Country films by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
        dslContext.selectFrom(FILM)
            .where(FILM.COUNTRY_ID.eq(country.id).andFilms(title, genre))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actor(
        country: CountryDto,
        @Argument id: Long
    ): ActorDto? = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(ACTOR.COUNTRY_ID.eq(country.id).and(ACTOR.ID.eq(id)))
            .fetchAny { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actors(
        country: CountryDto,
        @Argument firstName: String?,
        @Argument lastName: String?,
        @Argument birthdayFrom: LocalDate?,
        @Argument birthdayTo: LocalDate?,
        @Argument gender: Gender?,
        @Argument limit: Int,
        @Argument offset: Int
    ): List<ActorDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(ACTOR.COUNTRY_ID.eq(country.id).andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun taggable(
        country: CountryDto,
        @Argument tag: String
    ): List<TaggableDto> = hasAnyRole("USER", "ADMIN") {
        val result = mutableListOf<TaggableDto>()

        dslContext.selectFrom(FILM).where(FILM.COUNTRY_ID.eq(country.id).and(filmTagsContains(tag))).forEach {
            result.add(it.toDto())
        }

        dslContext.selectFrom(ACTOR).where(ACTOR.COUNTRY_ID.eq(country.id).and(actorTagsContains(tag))).forEach {
            result.add(it.toDto())
        }

        result
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun native(country: CountryDto): List<NativeDto> = hasAnyRole("USER", "ADMIN") {
        val result = mutableListOf<NativeDto>()

        dslContext.selectFrom(FILM).where(FILM.COUNTRY_ID.eq(country.id)).forEach {
            result.add(it.toDto())
        }

        dslContext.selectFrom(ACTOR).where(ACTOR.COUNTRY_ID.eq(country.id)).forEach {
            result.add(it.toDto())
        }

        result
    }
}