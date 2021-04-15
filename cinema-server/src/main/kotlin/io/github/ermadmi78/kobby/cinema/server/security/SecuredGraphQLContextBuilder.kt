package io.github.ermadmi78.kobby.cinema.server.security

import graphql.kickstart.spring.GraphQLSpringContext
import graphql.kickstart.spring.webflux.DefaultGraphQLSpringWebfluxContextBuilder
import graphql.kickstart.spring.webflux.GraphQLSpringWebSocketSessionContext
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.server.ServerWebExchange

/**
 * Created on 24.04.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class SecuredGraphQLContextBuilder(
    private val authorize: suspend (String) -> Authentication?
) : DefaultGraphQLSpringWebfluxContextBuilder() {
    override fun build(webSocketSession: WebSocketSession): GraphQLSpringWebSocketSessionContext {
        return super.build(webSocketSession)
    }

    override fun build(serverWebExchange: ServerWebExchange): GraphQLSpringContext =
        GraphQLSpringHttpScope(serverWebExchange) { http ->
            http.request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.let { authorize(it) }
        }
}