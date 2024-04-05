package io.github.ermadmi78.kobby.cinema.server.controller

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.server.eventbus.EventBus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.reactor.asFlux
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

/**
 * Created on 29.05.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Controller
@SchemaMapping(typeName = "Subscription")
class SubscriptionController(private val eventBus: EventBus) {
    @SchemaMapping
    suspend fun countryCreated(): Flux<CountryDto> = hasAnyRole("USER", "ADMIN") {
        println(
            "Subscription on country created by user [${authentication.name}] " +
                    "in thread [${Thread.currentThread().name}]"
        )
        eventBus.countryCreatedFlow().asFlux()
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun filmCreated(@Argument countryId: Long?): Flux<FilmDto> = hasAnyRole("USER", "ADMIN") {
        var filmCreatedFlow = eventBus.filmCreatedFlow()
        if (countryId != null) {
            filmCreatedFlow = filmCreatedFlow.filter {
                it.countryId == countryId
            }
        }

        filmCreatedFlow.asFlux()
    }

    //******************************************************************************************************************

    @SchemaMapping
    suspend fun actorCreated(@Argument countryId: Long?): Flux<ActorDto> = hasAnyRole("USER", "ADMIN") {
        var actorCreatedFlow = eventBus.actorCreatedFlow()
        if (countryId != null) {
            actorCreatedFlow = actorCreatedFlow.filter {
                it.countryId == countryId
            }
        }

        actorCreatedFlow.asFlux()
    }
}