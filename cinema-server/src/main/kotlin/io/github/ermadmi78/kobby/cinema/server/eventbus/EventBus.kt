package io.github.ermadmi78.kobby.cinema.server.eventbus

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

/**
 * Created on 29.05.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
@OptIn(DelicateCoroutinesApi::class)
class EventBus {
    private val countryCreatedBus: MutableSharedFlow<CountryDto> = MutableSharedFlow(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val filmCreatedBus: MutableSharedFlow<FilmDto> = MutableSharedFlow(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val actorCreatedBus: MutableSharedFlow<ActorDto> = MutableSharedFlow(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        GlobalScope.launch {
            countryCreatedBus.subscriptionCount.collect {
                println("### Country subscriptions count: $it")
            }
        }

        GlobalScope.launch {
            filmCreatedBus.subscriptionCount.collect {
                println("### Film subscriptions count: $it")
            }
        }

        GlobalScope.launch {
            actorCreatedBus.subscriptionCount.collect {
                println("### Actor subscriptions count: $it")
            }
        }
    }

    suspend fun fireCountryCreated(country: CountryDto) =
        countryCreatedBus.emit(country)

    fun countryCreatedFlow(): Flow<CountryDto> =
        countryCreatedBus.asSharedFlow()

    suspend fun fireFilmCreated(film: FilmDto) =
        filmCreatedBus.emit(film)

    fun filmCreatedFlow(): Flow<FilmDto> =
        filmCreatedBus.asSharedFlow()

    suspend fun fireActorCreated(actor: ActorDto) =
        actorCreatedBus.emit(actor)

    fun actorCreatedFlow(): Flow<ActorDto> =
        actorCreatedBus.asSharedFlow()
}