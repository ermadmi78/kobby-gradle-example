package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaMutationResolver
import io.github.ermadmi78.kobby.cinema.server.eventbus.EventBus
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class MutationResolver(
    private val dslContext: DSLContext,
    private val eventBus: EventBus
) : CinemaMutationResolver {
    override suspend fun createCountry(name: String): CountryDto = hasAnyRole("ADMIN") {
        println(
            "Mutation create country by user [${authentication.name}] " +
                    "in thread [${Thread.currentThread().name}]"
        )
        val newCountry = dslContext.insertInto(COUNTRY)
            .set(COUNTRY.NAME, name)
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireCountryCreated(newCountry)
        newCountry
    }

    override suspend fun createFilm(
        countryId: Long,
        film: FilmInput,
        tags: TagInput?
    ): FilmDto = hasAnyRole("ADMIN") {
        val newFilm = dslContext.insertInto(FILM)
            .set(FILM.COUNTRY_ID, countryId)
            .set(FILM.TITLE, film.title)
            .set(FILM.GENRE, film.genre.toRecord())
            .set(FILM.TAGS, tags?.value ?: "")
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireFilmCreated(newFilm)
        newFilm
    }

    override suspend fun createActor(
        countryId: Long,
        actor: ActorInput,
        tags: TagInput?
    ): ActorDto = hasAnyRole("ADMIN") {
        val newActor = dslContext.insertInto(ACTOR)
            .set(ACTOR.COUNTRY_ID, countryId)
            .set(ACTOR.FIRST_NAME, actor.firstName)
            .set(ACTOR.LAST_NAME, actor.lastName)
            .set(ACTOR.BIRTHDAY, actor.birthday)
            .set(ACTOR.GENDER, actor.gender.toRecord())
            .set(ACTOR.TAGS, tags?.value ?: "")
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireActorCreated(newActor)
        newActor
    }

    override suspend fun updateBirthday(
        actorId: Long,
        birthday: LocalDate
    ): ActorDto? = hasAnyRole("ADMIN") {
        dslContext.update(ACTOR)
            .set(ACTOR.BIRTHDAY, birthday)
            .where(ACTOR.ID.eq(actorId))
            .returning()
            .fetchOne()
            ?.toDto()
    }

    override suspend fun associate(
        filmId: Long,
        actorId: Long
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.insertInto(FILM_ACTOR)
            .set(FILM_ACTOR.FILM_ID, filmId)
            .set(FILM_ACTOR.ACTOR_ID, actorId)
            .onDuplicateKeyIgnore()
            .execute() == 1
    }

    override suspend fun tagFilm(
        filmId: Long,
        tagValue: String
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.update(FILM)
            .set(FILM.TAGS, DSL.function("CONCAT", FILM.TAGS.dataType, FILM.TAGS, DSL.`val`(tagValue)))
            .where(FILM.ID.eq(filmId))
            .and(filmTagsContains(tagValue).not())
            .execute() == 1
    }

    override suspend fun tagActor(
        actorId: Long,
        tagValue: String
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.update(ACTOR)
            .set(ACTOR.TAGS, DSL.function("CONCAT", ACTOR.TAGS.dataType, ACTOR.TAGS, DSL.`val`(tagValue)))
            .where(ACTOR.ID.eq(actorId))
            .and(actorTagsContains(tagValue).not())
            .execute() == 1
    }
}