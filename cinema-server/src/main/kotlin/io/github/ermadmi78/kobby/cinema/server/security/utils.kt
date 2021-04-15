package io.github.ermadmi78.kobby.cinema.server.security

import graphql.kickstart.tools.SchemaParserOptions.GenericWrapper
import graphql.kickstart.tools.util.ParameterizedTypeImpl
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * Created on 24.04.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class AuthorizedScope(val authentication: Authentication)

suspend fun <T> hasAnyRole(vararg roles: String, block: suspend AuthorizedScope.() -> T): T {
    if (roles.isEmpty()) {
        throw AccessDeniedException("Call denied")
    }

    val authentication = getAuthentication()
        ?: throw AccessDeniedException("Principal is not authorized")

    if (authentication.hasAnyRole(*roles)) {
        return AuthorizedScope(authentication).block()
    }

    throw AccessDeniedException("Not enough rights")
}

private const val ROLE_PREFIX = "ROLE_"

fun Authentication.hasAnyRole(vararg roles: String): Boolean = authorities.asSequence()
    .map { it.authority }
    .filterNotNull()
    .filter { it.startsWith(ROLE_PREFIX) }
    .map { it.substring(ROLE_PREFIX.length) }
    .filter { it.isNotEmpty() }
    .any { it in roles }

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getAuthentication(): Authentication? {
    val httpContext = coroutineContext[HttpContext.Key]
    if (httpContext != null) {
        return httpContext.getAuthentication()
    }

    val reactorContext = coroutineContext[ReactorContext.Key]
    if (reactorContext != null) {
        return reactorContext.context
            .getOrDefault<Mono<SecurityContext>>(SecurityContext::class.java, null)
            ?.awaitSingle()
            ?.authentication
    }

    return null
}

fun String.getBasicAuthentication(): Authentication? = takeIf { it.startsWith(BASIC, true) }
    ?.substring(BASIC.length)?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.decodeBase64()
    ?.split(':', limit = 2)
    ?.takeIf { it.size == 2 }
    ?.let { UsernamePasswordAuthenticationToken(it[0], it[1]) }

fun ServerWebExchange.getBasicAuthentication(): Authentication? =
    request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.getBasicAuthentication()

private const val BASIC = "Basic "

private fun String.decodeBase64(): String? = try {
    String(Base64.getDecoder().decode(this))
} catch (e: Exception) {
    null
}


fun createMonoGenericWrapper() = GenericWrapper.withTransformer(
    Mono::class, 0,
    { mono: Mono<*>, environment: DataFetchingEnvironment ->
        mono.contextWrite(
            ReactiveSecurityContextHolder.withSecurityContext(
                mono<SecurityContext>(environment.httpContext!!) {
                    SecurityContextImpl(coroutineContext[HttpContext]?.getAuthentication()!!)
                }
            )
        ).toFuture()
    }
)

fun createFluxGenericWrapper() = GenericWrapper.withTransformer(
    Flux::class, 0,
    { flux: Flux<*>, environment: DataFetchingEnvironment ->
        flux.contextWrite(
            ReactiveSecurityContextHolder.withSecurityContext(
                mono<SecurityContext>(environment.httpContext!!) {
                    SecurityContextImpl(coroutineContext[HttpContext.Key]?.getAuthentication()!!)
                }
            )
        ).collectList().toFuture()
    },
    { innerType -> ParameterizedTypeImpl.make(List::class.java, arrayOf(innerType), null) }
)

private val DataFetchingEnvironment.httpContext: HttpContext?
    get() = this.getContext<CoroutineScope>().coroutineContext[HttpContext]