package io.github.ermadmi78.kobby.cinema.server.resolvers

import graphql.kickstart.tools.GraphQLMutationResolver
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class MutationResolver : GraphQLMutationResolver {
    suspend fun createCountry(title: String): CountryDto = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }

    suspend fun createFilm(countryId: Long, film: FilmInput, tag: TagInput?): FilmDto = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }

    suspend fun createActor(countryId: Long, actor: ActorInput, tag: TagInput?): ActorDto = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }

    suspend fun associate(filmId: Long, actorId: Long): Boolean = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }

    suspend fun tagFilm(filmId: Long, tagValue: String): Boolean = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }

    suspend fun tagActor(actorId: Long, tagValue: String): Boolean = hasAnyRole("ADMIN") {
        TODO("Not implemented yet")
    }
}