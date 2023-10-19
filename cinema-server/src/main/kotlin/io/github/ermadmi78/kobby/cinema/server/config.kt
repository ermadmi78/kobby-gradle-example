package io.github.ermadmi78.kobby.cinema.server

import graphql.kickstart.spring.webflux.GraphQLSpringWebfluxContextBuilder
import graphql.kickstart.tools.CoroutineContextProvider
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import io.github.ermadmi78.kobby.cinema.server.resolvers.QueryResolver
import io.github.ermadmi78.kobby.cinema.server.scalars.JsonScalar
import io.github.ermadmi78.kobby.cinema.server.security.SecuredGraphQLContextBuilder
import io.github.ermadmi78.kobby.cinema.server.security.getBasicAuthentication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
class ApplicationConfiguration {
    /**
     * Static http resources for GraphIQL (http://localhost:8080/graphiql)
     */
    @Bean
    fun resourcesRouter() = resources("/**", ClassPathResource("static/"))

    /**
     * scalar JSON support
     */
    @Bean
    fun scalarJson(): GraphQLScalarType = JsonScalar.INSTANCE

    /**
     * scalar Date support
     */
    @Bean
    fun scalarDate(): GraphQLScalarType = ExtendedScalars.Date

    /**
     * Coroutine based resolvers dispatcher
     * @see: [QueryResolver.country]
     */
    @Bean
    fun resolverDispatcher(): CoroutineDispatcher = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        BasicThreadFactory.Builder()
            .namingPattern("resolver-thread-%d")
            .daemon(true)
            .build()
    ).asCoroutineDispatcher()

    /**
     * Coroutine dispatcher configuration
     */
    @Bean
    fun coroutineContextProvider(resolverDispatcher: CoroutineDispatcher): CoroutineContextProvider =
        object : CoroutineContextProvider {
            override fun provide(): CoroutineContext {
                return resolverDispatcher
            }
        }

    /**
     * Reactive Basic HTTP security configuration
     *
     * Apollo Client does not use HTTP headers to send authentication token.
     * Instead it sends token in payload of GQL_CONNECTION_INIT message.
     * So, we have to switch off Spring Boot authentication checking of 'subscriptions' endpoint
     * and authenticate subscription manually in [SecuredGraphQLContextBuilder]
     *
     * See:
     *
     * https://www.apollographql.com/docs/react/data/subscriptions/#5-authenticate-over-websocket-optional
     *
     * https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
     */
    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf {
                it.disable()
            }
            .authorizeExchange {
                // Permit GraphQL Playground (http://localhost:8080/playground)
                it.pathMatchers("/playground").permitAll()

                // Permit GraphQL Voyager (http://localhost:8080/voyager)
                it.pathMatchers("/voyager").permitAll()

                // Permit GraphIQL (http://localhost:8080/graphiql)
                it.pathMatchers("/graphiql").permitAll()

                // Permit GraphIQL static resources
                it.pathMatchers("/vendor/**").permitAll()

                // See 'securityFilterChain` method documentation
                it.pathMatchers("/subscriptions").permitAll()

                it.anyExchange().authenticated()
            }
            .httpBasic {}
            .formLogin {
                it.disable()
            }
            .build()
    }

    /**
     * Basic HTTP users configuration
     */
    @Bean
    fun userDetailsService(): ReactiveUserDetailsService {
        val user: UserDetails = User.builder()
            .username("user")
            .password("{noop}user")
            .roles("USER")
            .build()
        val admin: UserDetails = User.builder()
            .username("admin")
            .password("{noop}admin")
            .roles("ADMIN")
            .build()
        return MapReactiveUserDetailsService(user, admin)
    }

    /**
     * GraphQL security configuration
     */
    @Bean
    fun contextBuilder(userDetailsService: ReactiveUserDetailsService): GraphQLSpringWebfluxContextBuilder {
        return SecuredGraphQLContextBuilder { header ->
            header.getBasicAuthentication()
                ?.let { userDetailsService.findByUsername(it.name).awaitSingle() }
                ?.let { UsernamePasswordAuthenticationToken(it, it.password, it.authorities) }
        }
    }
}
