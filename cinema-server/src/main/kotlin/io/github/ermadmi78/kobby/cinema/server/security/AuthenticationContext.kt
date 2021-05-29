package io.github.ermadmi78.kobby.cinema.server.security

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.security.core.Authentication
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Created on 03.06.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
class AuthenticationContext(
    private val provider: suspend () -> Authentication? = { null }
) : AbstractCoroutineContextElement(AuthenticationContext) {
    companion object Key : CoroutineContext.Key<AuthenticationContext>

    private val mutex = Mutex()
    private var provided = false
    private var authentication: Authentication? = null

    suspend fun getAuthentication(): Authentication? = mutex.withLock {
        if (!provided) {
            authentication = provider()
            provided = true
        }

        authentication
    }
}