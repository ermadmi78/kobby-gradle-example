package io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.createActor
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.createFilm
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorInput
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmInput

/**
 * Created on 13.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

suspend fun Country.refresh(__projection: (CountryProjection.() -> Unit)? = null): Country = query {
    country(id) {
        __projection?.invoke(this) ?: __withCurrentProjection()
    }
}.country!!

suspend fun Country.findFilm(id: Long, __projection: FilmProjection.() -> Unit = {}): Film? = refresh {
    __minimize()
    film(id, __projection)
}.film

suspend fun Country.fetchFilm(id: Long, __projection: FilmProjection.() -> Unit = {}): Film =
    findFilm(id, __projection)!!

suspend fun Country.findFilms(__query: CountryFilmsQuery.() -> Unit = {}): List<Film> = refresh {
    __minimize()
    films(__query)
}.films

//**********************************************************************************************************************

suspend fun Country.createFilm(film: FilmInput, __query: MutationCreateFilmQuery.() -> Unit = {}): Film =
    createFilm(id, film, __query)

suspend fun Country.createActor(actor: ActorInput, __query: MutationCreateActorQuery.() -> Unit = {}): Actor =
    createActor(id, actor, __query)