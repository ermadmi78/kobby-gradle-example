package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.ACTOR
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.FILM
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class CountryResolver : GraphQLResolver<CountryDto> {
    companion object {
        private val ALL_FIELDS = setOf("id", "name")
    }

    @Autowired
    private lateinit var dslContext: DSLContext

    suspend fun fields(
        country: CountryDto,
        keys: List<String>?
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> result[it] = country.id
                "name" -> result[it] = country.name
            }
        }

        return result
    }

    suspend fun film(
        country: CountryDto,
        id: Long
    ): FilmDto? = dslContext.selectFrom(FILM)
        .where(FILM.COUNTRY_ID.eq(country.id).and(FILM.ID.eq(id)))
        .fetchAny { it.toDto() }

    suspend fun films(
        country: CountryDto,
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = dslContext.selectFrom(FILM)
        .where(FILM.COUNTRY_ID.eq(country.id).andFilms(title, genre))
        .limit(offset.prepare(), limit.prepare())
        .fetch { it.toDto() }

    suspend fun actor(
        country: CountryDto,
        id: Long
    ): ActorDto? = dslContext.selectFrom(ACTOR)
        .where(ACTOR.COUNTRY_ID.eq(country.id).and(ACTOR.ID.eq(id)))
        .fetchAny { it.toDto() }

    suspend fun actors(
        country: CountryDto,
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = dslContext.selectFrom(ACTOR)
        .where(ACTOR.COUNTRY_ID.eq(country.id).andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
        .limit(offset.prepare(), limit.prepare())
        .fetch { it.toDto() }

    suspend fun taggable(
        country: CountryDto,
        tag: String
    ): List<TaggableDto> {
        val result = mutableListOf<TaggableDto>()

        dslContext.selectFrom(FILM).where(FILM.COUNTRY_ID.eq(country.id).and(filmTagsContains(tag))).forEach {
            result.add(it.toDto())
        }

        dslContext.selectFrom(ACTOR).where(ACTOR.COUNTRY_ID.eq(country.id).and(actorTagsContains(tag))).forEach {
            result.add(it.toDto())
        }

        return result
    }

    suspend fun native(country: CountryDto): List<NativeDto> {
        val result = mutableListOf<NativeDto>()

        dslContext.selectFrom(FILM).where(FILM.COUNTRY_ID.eq(country.id)).forEach {
            result.add(it.toDto())
        }

        dslContext.selectFrom(ACTOR).where(ACTOR.COUNTRY_ID.eq(country.id)).forEach {
            result.add(it.toDto())
        }

        return result
    }
}