package io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity

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
    films(__query)
}.films