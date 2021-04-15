package io.github.ermadmi78.kobby.cinema.server.security

import org.springframework.security.core.Authentication
import org.springframework.web.server.ServerWebExchange
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Created on 24.04.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class HttpContext(
    private val _http: ServerWebExchange,
    private val extractor: suspend (ServerWebExchange) -> Authentication? = { null }
) : AbstractCoroutineContextElement(HttpContext) {
    companion object Key : CoroutineContext.Key<HttpContext>

    val http: ServerWebExchange get() = _http
    suspend fun getAuthentication(): Authentication? = extractor(_http)
}