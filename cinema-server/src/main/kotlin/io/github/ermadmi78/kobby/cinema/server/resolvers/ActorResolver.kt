package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Genre
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaActorResolver
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class ActorResolver(private val dslContext: DSLContext) : CinemaActorResolver {
    companion object {
        private val ALL_FIELDS = setOf("id", "firstName", "lastName", "birthday", "gender")
    }

    override suspend fun fields(
        actor: ActorDto,
        keys: List<String>?
    ): JsonObject = buildJsonObject {
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> put(it, actor.id)
                "firstName" -> put(it, actor.firstName)
                "lastName" -> put(it, actor.lastName)
                "birthday" -> put(it, actor.birthday.toString())
                "gender" -> put(it, actor.gender?.name)
            }
        }
    }

    override suspend fun country(actor: ActorDto): CountryDto = hasAnyRole("USER", "ADMIN") {
        dslContext.selectFrom(COUNTRY)
            .where(COUNTRY.ID.eq(actor.countryId))
            .fetchAny { it.toDto() }!!
    }

    override suspend fun films(
        actor: ActorDto,
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = hasAnyRole("USER", "ADMIN") {
        dslContext.select(FILM.asterisk())
            .from(FILM)
            .join(FILM_ACTOR)
            .onKey()
            .where(FILM_ACTOR.ACTOR_ID.eq(actor.id))
            .and(trueCondition().andFilms(title, genre))
            .limit(offset.prepare(), limit.prepare())
            .fetch {
                it.into(FILM).toDto()
            }
    }
}