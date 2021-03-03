package io.github.ermadmi78.kobby.cinema.kotlin.client

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.MutationDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.QueryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaException
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaMutationResult
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaQueryResult
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaRequest
import io.ktor.client.*
import io.ktor.client.request.*

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

class CinemaKtorAdapter(private val client: HttpClient) : CinemaAdapter {
    override suspend fun executeQuery(query: String, variables: Map<String, Any?>): QueryDto {
        println("Query: $query")
        println("Variables: $variables")

        val request = CinemaRequest(query, variables)
        val result = client.post<CinemaQueryResult> {
            body = request
        }

        result.errors?.takeIf { it.isNotEmpty() }?.let {
            throw CinemaException("GraphQL query failed", request, it)
        }
        return result.data ?: throw CinemaException(
            "GraphQL query completes successfully but returns no data",
            request
        )
    }

    override suspend fun executeMutation(query: String, variables: Map<String, Any?>): MutationDto {
        println("Query: $query")
        println("Variables: $variables")

        val request = CinemaRequest(query, variables)
        val result = client.post<CinemaMutationResult> {
            body = request
        }

        result.errors?.takeIf { it.isNotEmpty() }?.let {
            throw CinemaException("GraphQL mutation failed", request, it)
        }
        return result.data ?: throw CinemaException(
            "GraphQL mutation completes successfully but returns no data",
            request
        )
    }
}