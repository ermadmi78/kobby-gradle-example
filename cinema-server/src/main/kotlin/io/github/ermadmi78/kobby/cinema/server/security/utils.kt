package io.github.ermadmi78.kobby.cinema.server.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
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

suspend fun getAuthentication(): Authentication? =
    coroutineContext[AuthenticationContext.Key]?.getAuthentication()

fun String.getBasicAuthentication(): Authentication? = takeIf { it.startsWith(BASIC, true) }
    ?.substring(BASIC.length)?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.decodeBase64()
    ?.split(':', limit = 2)
    ?.takeIf { it.size == 2 }
    ?.let { UsernamePasswordAuthenticationToken(it[0], it[1]) }

private const val BASIC = "Basic "

private fun String.decodeBase64(): String? = try {
    String(Base64.getDecoder().decode(this))
} catch (e: Exception) {
    null
}
