package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.Condition
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
@SchemaMapping(typeName = "Query")
class QueryController(private val dslContext: DSLContext) {
    @SchemaMapping
    suspend fun country(@Argument id: Long): CountryDto? = hasAnyRole("USER", "ADMIN") {
        println("Query country by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun countries(
        @Argument name: String?,
        @Argument limit: Int,
        @Argument offset: Int
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

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun film(@Argument id: Long): FilmDto? = hasAnyRole("USER", "ADMIN") {
        println("Query film by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
        dslContext.selectFrom(FILM)
            .where(FILM.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun films(
        @Argument title: String?,
        @Argument genre: Genre?,
        @Argument limit: Int,
        @Argument offset: Int
    ): List<FilmDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(FILM)
            .where(trueCondition().andFilms(title, genre))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actor(@Argument id: Long): ActorDto? = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(ACTOR.ID.eq(id))
            .fetchAny { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actors(
        @Argument firstName: String?,
        @Argument lastName: String?,
        @Argument birthdayFrom: LocalDate?,
        @Argument birthdayTo: LocalDate?,
        @Argument gender: Gender?,
        @Argument limit: Int,
        @Argument offset: Int
    ): List<ActorDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(ACTOR)
            .where(trueCondition().andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun taggable(@Argument tag: String): List<TaggableDto> = hasAnyRole("USER", "ADMIN") {
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