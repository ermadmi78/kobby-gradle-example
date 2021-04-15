package io.github.ermadmi78.kobby.cinema.server.security

import graphql.kickstart.spring.GraphQLSpringServerWebExchangeContext
import kotlinx.coroutines.CoroutineScope
import org.springframework.security.core.Authentication
import org.springframework.web.server.ServerWebExchange

/**
 * Created on 24.04.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class GraphQLSpringHttpScope(
    serverWebExchange: ServerWebExchange,
    extractor: suspend (ServerWebExchange) -> Authentication? = { null }
) : GraphQLSpringServerWebExchangeContext(serverWebExchange),
    CoroutineScope by CoroutineScope(HttpContext(serverWebExchange, extractor))