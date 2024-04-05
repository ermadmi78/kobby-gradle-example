package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.eventbus.EventBus
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Controller
@SchemaMapping(typeName = "Mutation")
class MutationController(
    private val dslContext: DSLContext,
    private val eventBus: EventBus
) {
    @SchemaMapping
    suspend fun createCountry(@Argument name: String): CountryDto = hasAnyRole("ADMIN") {
        println(
            "Mutation create country by user [${authentication.name}] " +
                    "in thread [${Thread.currentThread().name}]"
        )
        dslContext.insertInto(COUNTRY)
            .set(COUNTRY.NAME, name)
            .returning()
            .fetchOne()!!
            .toDto()
            .also {
                eventBus.fireCountryCreated(it)
            }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun createFilm(
        @Argument countryId: Long,
        @Argument film: FilmInput,
        @Argument tags: TagInput?
    ): FilmDto = hasAnyRole("ADMIN") {
        dslContext.insertInto(FILM)
            .set(FILM.COUNTRY_ID, countryId)
            .set(FILM.TITLE, film.title)
            .set(FILM.GENRE, film.genre.toRecord())
            .set(FILM.TAGS, tags?.value ?: "")
            .returning()
            .fetchOne()!!
            .toDto()
            .also {
                eventBus.fireFilmCreated(it)
            }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun createActor(
        @Argument countryId: Long,
        @Argument actor: ActorInput,
        @Argument tags: TagInput?
    ): ActorDto = hasAnyRole("ADMIN") {
        dslContext.insertInto(ACTOR)
            .set(ACTOR.COUNTRY_ID, countryId)
            .set(ACTOR.FIRST_NAME, actor.firstName)
            .set(ACTOR.LAST_NAME, actor.lastName)
            .set(ACTOR.BIRTHDAY, actor.birthday)
            .set(ACTOR.GENDER, actor.gender.toRecord())
            .set(ACTOR.TAGS, tags?.value ?: "")
            .returning()
            .fetchOne()!!
            .toDto()
            .also {
                eventBus.fireActorCreated(it)
            }
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun updateBirthday(
        @Argument actorId: Long,
        @Argument birthday: LocalDate
    ): ActorDto? = hasAnyRole("ADMIN") {
        dslContext.update(ACTOR)
            .set(ACTOR.BIRTHDAY, birthday)
            .where(ACTOR.ID.eq(actorId))
            .returning()
            .fetchOne()
            ?.toDto()
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun associate(
        @Argument filmId: Long,
        @Argument actorId: Long
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.insertInto(FILM_ACTOR)
            .set(FILM_ACTOR.FILM_ID, filmId)
            .set(FILM_ACTOR.ACTOR_ID, actorId)
            .onDuplicateKeyIgnore()
            .execute() == 1
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun tagFilm(
        @Argument filmId: Long,
        @Argument tagValue: String
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.update(FILM)
            .set(FILM.TAGS, DSL.function("CONCAT", FILM.TAGS.dataType, FILM.TAGS, DSL.`val`(tagValue)))
            .where(FILM.ID.eq(filmId))
            .and(filmTagsContains(tagValue).not())
            .execute() == 1
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun tagActor(
        @Argument actorId: Long,
        @Argument tagValue: String
    ): Boolean = hasAnyRole("ADMIN") {
        dslContext.update(ACTOR)
            .set(ACTOR.TAGS, DSL.function("CONCAT", ACTOR.TAGS.dataType, ACTOR.TAGS, DSL.`val`(tagValue)))
            .where(ACTOR.ID.eq(actorId))
            .and(actorTagsContains(tagValue).not())
            .execute() == 1
    }
}