package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.server.jooq.enums.ActorGender
import io.github.ermadmi78.kobby.cinema.server.jooq.enums.FilmGenre
import io.github.ermadmi78.kobby.cinema.server.jooq.tables.records.ActorRecord
import io.github.ermadmi78.kobby.cinema.server.jooq.tables.records.CountryRecord
import io.github.ermadmi78.kobby.cinema.server.jooq.tables.records.FilmRecord

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

fun CountryRecord.toDto() = CountryDto {
    id = getId()
    name = getName()
}

fun FilmRecord.toDto() = FilmDto {
    id = getId()
    countryId = getCountryId()
    title = getTitle()
    genre = getGenre()?.toDto()
    tags = getTags()
        ?.splitToSequence(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.map { TagDto(it) }
        ?.toList()
        ?: listOf()
}

fun ActorRecord.toDto() = ActorDto {
    id = getId()
    countryId = getCountryId()
    firstName = getFirstName()
    lastName = getLastName()
    birthday = getBirthday()
    gender = getGender()?.toDto()
    tags = getTags()
        ?.splitToSequence(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.map { TagDto(it) }
        ?.toList()
        ?: listOf()
}

fun FilmGenre.toDto(): Genre = Genre.valueOf(name)
fun Genre.toRecord(): FilmGenre = FilmGenre.valueOf(name)

fun ActorGender.toDto(): Gender = Gender.valueOf(name)
fun Gender.toRecord(): ActorGender = ActorGender.valueOf(name)

fun Int.prepare(): Int? = if (this > 0) this else null