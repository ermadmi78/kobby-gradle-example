package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class QueryResolver : GraphQLQueryResolver {
    @Autowired
    private lateinit var dslContext: DSLContext

    suspend fun country(id: Long): CountryDto? = dslContext.selectFrom(COUNTRY)
        .where(COUNTRY.ID.eq(id))
        .fetchAny { it.toDto() }

    suspend fun countries(
        name: String?,
        limit: Int,
        offset: Int
    ): List<CountryDto> {
        var condition: Condition = DSL.trueCondition()

        if (!name.isNullOrBlank()) {
            condition = condition.and(COUNTRY.NAME.containsIgnoreCase(name.trim()))
        }

        return dslContext.selectFrom(COUNTRY)
            .where(condition)
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    suspend fun film(id: Long): FilmDto? = dslContext.selectFrom(FILM)
        .where(FILM.ID.eq(id))
        .fetchAny { it.toDto() }

    suspend fun films(
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> {
        var condition: Condition = DSL.trueCondition()

        if (!title.isNullOrBlank()) {
            condition = condition.and(FILM.TITLE.containsIgnoreCase(title.trim()))
        }
        if (genre != null) {
            condition = condition.and(FILM.GENRE.eq(genre.toRecord()))
        }

        return dslContext.selectFrom(FILM)
            .where(condition)
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    suspend fun actor(id: Long): ActorDto? = dslContext.selectFrom(ACTOR)
        .where(ACTOR.ID.eq(id))
        .fetchAny { it.toDto() }

    suspend fun actors(
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> {
        var condition: Condition = DSL.trueCondition()

        if (!firstName.isNullOrBlank()) {
            condition = condition.and(ACTOR.FIRST_NAME.containsIgnoreCase(firstName.trim()))
        }
        if (!lastName.isNullOrBlank()) {
            condition = condition.and(ACTOR.LAST_NAME.containsIgnoreCase(lastName.trim()))
        }
        if (birthdayFrom != null) {
            condition = condition.and(ACTOR.BIRTHDAY.ge(birthdayFrom))
        }
        if (birthdayTo != null) {
            condition = condition.and(ACTOR.BIRTHDAY.le(birthdayTo))
        }
        if (gender != null) {
            condition = condition.and(ACTOR.GENDER.eq(gender.toRecord()))
        }

        return dslContext.selectFrom(ACTOR)
            .where(condition)
            .limit(offset.prepare(), limit.prepare())
            .fetch { it.toDto() }
    }

    suspend fun taggable(tag: String): List<TaggableDto> {
        val result = mutableListOf<TaggableDto>()

        dslContext.selectFrom(FILM)
            .where(DSL.field("ARRAY_CONTAINS({0}, {1})", Boolean::class.java, FILM.TAGS, tag).eq(true))
            .forEach {
                result.add(it.toDto())
            }

        dslContext.selectFrom(ACTOR)
            .where(DSL.field("ARRAY_CONTAINS({0}, {1})", Boolean::class.java, ACTOR.TAGS, tag).eq(true))
            .forEach {
                result.add(it.toDto())
            }

        return result
    }
}