package io.github.ermadmi78.kobby.cinema.server

import graphql.kickstart.tools.CoroutineContextProvider
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.coroutines.CoroutineContext

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

@Configuration
class ApplicationConfiguration {
    @Bean
    fun scalarJson(): GraphQLScalarType = ExtendedScalars.Json

    @Bean
    fun scalarDate(): GraphQLScalarType = ExtendedScalars.Date

    @Bean
    fun coroutineContextProvider(): CoroutineContextProvider = object : CoroutineContextProvider {
        override fun provide(): CoroutineContext {
            return Dispatchers.IO
        }
    }
}
