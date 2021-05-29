package io.github.ermadmi78.kobby.cinema.server.security

import graphql.kickstart.spring.webflux.DefaultGraphQLSpringWebSocketSessionContext
import kotlinx.coroutines.CoroutineScope
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.socket.WebSocketSession

/**
 * Created on 03.06.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class GraphQLSpringWebSocketSessionScope(
    webSocketSession: WebSocketSession,
    provider: suspend () -> Authentication? = { null }
) : DefaultGraphQLSpringWebSocketSessionContext(webSocketSession),
    CoroutineScope by CoroutineScope(AuthenticationContext(provider))