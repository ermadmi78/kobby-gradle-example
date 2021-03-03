package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Genre
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.TagDto
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class ActorResolver : GraphQLResolver<ActorDto> {
    suspend fun fields(
        actor: ActorDto,
        keys: List<String>?
    ): Map<String, Any?> = TODO()

    suspend fun tags(actor: ActorDto): List<TagDto> = TODO()

    suspend fun films(
        actor: ActorDto,
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = TODO()
}