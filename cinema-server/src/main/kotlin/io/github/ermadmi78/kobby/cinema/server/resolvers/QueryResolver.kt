package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.Taggable
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.COUNTRY
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
    ): List<CountryDto> = dslContext.selectFrom(COUNTRY).apply {
        if (name != null) {
            where(COUNTRY.NAME.equalIgnoreCase(name))
        }
        if (limit > 0) {
            limit(limit)
        }
        if (offset > 0) {
            offset(offset)
        }
    }.fetch { it.toDto() }

    suspend fun film(id: Long): FilmDto? = TODO()

    suspend fun films(
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = TODO()

    suspend fun actor(id: Long): ActorDto? = TODO()

    suspend fun actors(
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = TODO()

    suspend fun taggable(tag: String): List<Taggable> = TODO()
}