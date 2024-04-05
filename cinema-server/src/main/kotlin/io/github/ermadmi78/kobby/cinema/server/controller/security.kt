package io.github.ermadmi78.kobby.cinema.server.controller

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder

/**
 * Created on 05.04.2024
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

suspend fun getAuthentication(): Authentication? =
    ReactiveSecurityContextHolder.getContext().awaitSingle()?.authentication