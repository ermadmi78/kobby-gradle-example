package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLMutationResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class MutationResolver : GraphQLMutationResolver {
    suspend fun createCountry(title: String): CountryDto = TODO()

    suspend fun createFilm(countryId: Long, film: FilmInput, tag: TagInput?): FilmDto = TODO()

    suspend fun createActor(countryId: Long, actor: ActorInput, tag: TagInput?): ActorDto = TODO()

    suspend fun associate(filmId: Long, actorId: Long): Boolean = TODO()

    suspend fun tagFilm(filmId: Long, tagValue: String): Boolean = TODO()

    suspend fun tagActor(actorId: Long, tagValue: String): Boolean = TODO()
}