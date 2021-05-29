package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaSubscriptionResolver
import io.github.ermadmi78.kobby.cinema.server.eventbus.EventBus
import io.github.ermadmi78.kobby.cinema.server.security.hasAnyRole
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component

/**
 * Created on 29.05.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class SubscriptionResolver(private val eventBus: EventBus) : CinemaSubscriptionResolver {
    override suspend fun countryCreated(): Publisher<CountryDto> = hasAnyRole("USER", "ADMIN") {
        println(
            "Subscription on country created by user [${authentication.name}] " +
                    "in thread [${Thread.currentThread().name}]"
        )
        eventBus.countryCreatedFlow().asPublisher()
    }

    override suspend fun filmCreated(countryId: Long?): Publisher<FilmDto> = hasAnyRole("USER", "ADMIN") {
        var filmCreatedFlow = eventBus.filmCreatedFlow()
        if (countryId != null) {
            filmCreatedFlow = filmCreatedFlow.filter {
                it.countryId == countryId
            }
        }

        filmCreatedFlow.asPublisher()
    }

    override suspend fun actorCreated(countryId: Long?): Publisher<ActorDto> = hasAnyRole("USER", "ADMIN") {
        var actorCreatedFlow = eventBus.actorCreatedFlow()
        if (countryId != null) {
            actorCreatedFlow = actorCreatedFlow.filter {
                it.countryId == countryId
            }
        }

        actorCreatedFlow.asPublisher()
    }
}