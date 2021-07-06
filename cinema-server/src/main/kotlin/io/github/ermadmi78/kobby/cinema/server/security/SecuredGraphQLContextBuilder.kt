package io.github.ermadmi78.kobby.cinema.server.security

import graphql.kickstart.execution.subscriptions.SubscriptionSession
import graphql.kickstart.execution.subscriptions.apollo.ApolloSubscriptionConnectionListener
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage
import graphql.kickstart.spring.GraphQLSpringContext
import graphql.kickstart.spring.webflux.DefaultGraphQLSpringWebfluxContextBuilder
import graphql.kickstart.spring.webflux.GraphQLSpringWebSocketSessionContext
import graphql.kickstart.spring.webflux.ReactiveWebSocketSubscriptionSession
import graphql.kickstart.spring.webflux.apollo.ReactiveApolloSubscriptionSession
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
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
) : DefaultGraphQLSpringWebfluxContextBuilder(), ApolloSubscriptionConnectionListener {

    //******************************************************************************************************************
    //                                      Web Socket Authentication
    //******************************************************************************************************************

    companion object {
        private const val HTTP_HEADERS_AUTH_TOKEN_NAME = "Authorization"
        private const val APOLLO_AUTH_TOKEN_NAME = "authToken"
        private const val PRE_AUTHORIZED_AUTHENTICATION = "PRE_AUTHORIZED_AUTHENTICATION"
    }

    /**
     * Apollo Client does not use HTTP headers to send authentication token.
     * Instead it sends token in payload of GQL_CONNECTION_INIT message.
     * So, we have to switch off Spring Boot authentication checking and authenticate subscription manually
     * See:
     * https://www.apollographql.com/docs/react/data/subscriptions/#5-authenticate-over-websocket-optional
     * https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
     */
    override fun onConnect(session: SubscriptionSession, message: OperationMessage) {
        val token = message.authToken ?: when (session) {
            is ReactiveApolloSubscriptionSession ->
                session.unwrap().authToken
            is ReactiveWebSocketSubscriptionSession ->
                session.unwrap().authToken
            else -> null
        } ?: throw AccessDeniedException("Principal is not authorized")

        // Unfortunately, we have to resolve principal authentication in blocking context :(
        val authentication = runBlocking {
            authorize(token)
        } ?: throw AccessDeniedException("Principal is not authorized")

        session.userProperties[PRE_AUTHORIZED_AUTHENTICATION] = authentication
    }

    private val OperationMessage?.authToken: String?
        get() = (this?.payload as? Map<*, *>)?.let {
            (it[HTTP_HEADERS_AUTH_TOKEN_NAME] ?: it[APOLLO_AUTH_TOKEN_NAME]) as? String
        }

    private val WebSocketSession?.authToken: String?
        get() = this?.handshakeInfo?.headers?.get(HttpHeaders.AUTHORIZATION)?.firstOrNull()

    override fun build(webSocketSession: WebSocketSession): GraphQLSpringWebSocketSessionContext {
        val authentication: Authentication =
            (webSocketSession.attributes[PRE_AUTHORIZED_AUTHENTICATION] as? Authentication)
                ?: throw AccessDeniedException("Principal is not authorized")

        return GraphQLSpringWebSocketSessionScope(webSocketSession) {
            authentication
        }
    }

    //******************************************************************************************************************
    //                                      HTTP Authentication
    //******************************************************************************************************************

    override fun build(serverWebExchange: ServerWebExchange): GraphQLSpringContext =
        GraphQLSpringServerWebExchangeScope(serverWebExchange) {
            serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.let {
                authorize(it)
            }
        }
}