package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.TagDto
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class FilmResolver : GraphQLResolver<FilmDto> {
    suspend fun fields(
        film: FilmDto,
        keys: List<String>?
    ): Map<String, Any?> = TODO()

    suspend fun tags(film: FilmDto): List<TagDto> = TODO()

    suspend fun actors(
        film: FilmDto,
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = TODO()
}