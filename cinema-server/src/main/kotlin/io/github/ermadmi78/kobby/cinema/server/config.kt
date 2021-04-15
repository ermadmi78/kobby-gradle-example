package io.github.ermadmi78.kobby.cinema.server

import graphql.kickstart.spring.webflux.GraphQLSpringWebfluxContextBuilder
import graphql.kickstart.tools.CoroutineContextProvider
import graphql.kickstart.tools.SchemaParserOptions.GenericWrapper
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import io.github.ermadmi78.kobby.cinema.server.resolvers.QueryResolver
import io.github.ermadmi78.kobby.cinema.server.security.SecuredGraphQLContextBuilder
import io.github.ermadmi78.kobby.cinema.server.security.createFluxGenericWrapper
import io.github.ermadmi78.kobby.cinema.server.security.createMonoGenericWrapper
import io.github.ermadmi78.kobby.cinema.server.security.getBasicAuthentication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
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
@EnableReactiveMethodSecurity
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
    fun scalarJson(): GraphQLScalarType = ExtendedScalars.Json

    /**
     * scalar Date support
     */
    @Bean
    fun scalarDate(): GraphQLScalarType = ExtendedScalars.Date

    /**
     * Mono based resolvers support
     * @see: [QueryResolver.film]
     */
    @Bean
    fun monoGenericWrapper(): GenericWrapper = createMonoGenericWrapper()

    /**
     * Flux based resolvers support
     * @see: [QueryResolver.countries]
     */
    @Bean
    fun fluxGenericWrapper(): GenericWrapper = createFluxGenericWrapper()

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
     */
    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/graphiql").permitAll()  // Permit GraphIQL (http://localhost:8080/graphiql)
            .pathMatchers("/vendor/**").permitAll() // Permit GraphIQL static resources
            .anyExchange().authenticated()
            .and()
            .httpBasic()
            .and()
            .formLogin().disable()
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
