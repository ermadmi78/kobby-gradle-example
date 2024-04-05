package io.github.ermadmi78.kobby.cinema.server

import graphql.scalars.ExtendedScalars
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.ClassNameTypeResolver
import org.springframework.graphql.execution.GraphQlSource.SchemaResourceBuilder
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux


/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
class ApplicationConfiguration {
    @Bean
    fun sourceBuilderCustomizer() = GraphQlSourceBuilderCustomizer { builder: SchemaResourceBuilder ->
        builder.defaultTypeResolver(ClassNameTypeResolver().apply {
            setClassNameExtractor { klass: Class<*> ->
                klass.simpleName.removeSuffix("Dto")
            }
        })
    }

    @Bean
    fun runtimeWiringConfigurer() = RuntimeWiringConfigurer { builder ->
        builder
            .scalar(ExtendedScalars.Json) // scalar JSON support
            .scalar(ExtendedScalars.Date) // scalar Date support
    }

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
        .csrf { it.disable() }
        .authorizeExchange { it.anyExchange().authenticated() }
        .httpBasic {}
        .build()

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
}
