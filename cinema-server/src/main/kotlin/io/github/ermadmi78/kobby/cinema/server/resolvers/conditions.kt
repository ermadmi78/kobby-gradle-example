package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Genre
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.ACTOR
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.FILM
import org.jooq.Condition
import org.jooq.impl.DSL
import java.time.LocalDate

/**
 * Created on 04.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

fun Condition.andFilms(
    title: String?,
    genre: Genre?
): Condition {
    var result = this

    if (!title.isNullOrBlank()) {
        result = result.and(FILM.TITLE.containsIgnoreCase(title.trim()))
    }
    if (genre != null) {
        result = result.and(FILM.GENRE.eq(genre.toRecord()))
    }

    return result
}

fun Condition.andActors(
    firstName: String?,
    lastName: String?,
    birthdayFrom: LocalDate?,
    birthdayTo: LocalDate?,
    gender: Gender?
): Condition {
    var result: Condition = this

    if (!firstName.isNullOrBlank()) {
        result = result.and(ACTOR.FIRST_NAME.containsIgnoreCase(firstName.trim()))
    }
    if (!lastName.isNullOrBlank()) {
        result = result.and(ACTOR.LAST_NAME.containsIgnoreCase(lastName.trim()))
    }
    if (birthdayFrom != null) {
        result = result.and(ACTOR.BIRTHDAY.ge(birthdayFrom))
    }
    if (birthdayTo != null) {
        result = result.and(ACTOR.BIRTHDAY.le(birthdayTo))
    }
    if (gender != null) {
        result = result.and(ACTOR.GENDER.eq(gender.toRecord()))
    }

    return result
}

fun filmTagsContains(tag: String): Condition =
    DSL.field("ARRAY_CONTAINS({0}, {1})", Boolean::class.java, FILM.TAGS, tag).eq(true)

fun actorTagsContains(tag: String): Condition =
    DSL.field("ARRAY_CONTAINS({0}, {1})", Boolean::class.java, ACTOR.TAGS, tag).eq(true)