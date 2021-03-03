package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity.Taggable
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class CountryResolver : GraphQLResolver<CountryDto> {
    suspend fun fields(
        country: CountryDto,
        keys: List<String>?
    ): Map<String, Any?> = TODO()

    suspend fun film(
        country: CountryDto,
        id: Long
    ): FilmDto? = TODO()

    suspend fun films(
        country: CountryDto,
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = TODO()

    suspend fun actor(
        country: CountryDto,
        id: Long
    ): ActorDto? = TODO()

    suspend fun actors(
        country: CountryDto,
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = TODO()

    suspend fun taggable(
        country: CountryDto,
        tag: String
    ): List<Taggable> = TODO()

    suspend fun native(
        country: CountryDto,
        limit: Int,
        offset: Int
    ): List<NativeDto> = TODO()
}