package io.github.ermadmi78.kobby.cinema.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.MutationDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.QueryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaException
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaMutationResult
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaQueryResult
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.graphql.CinemaRequest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@Suppress("BlockingMethodInNonBlockingContext")
class CinemaTestAdapter(
    private val client: WebTestClient,
    private val mapper: ObjectMapper
) : CinemaAdapter {
    override suspend fun executeQuery(query: String, variables: Map<String, Any?>): QueryDto {
        val request = CinemaRequest(query, variables)
        return client.post()
            .uri("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Basic YWRtaW46YWRtaW4=")
            .bodyValue(mapper.writeValueAsString(request))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { mapper.readValue(it, CinemaQueryResult::class.java) }
            .let { result ->
                result.errors?.takeIf { it.isNotEmpty() }?.let {
                    throw CinemaException("GraphQL query failed", request, it)
                }
                result.data ?: throw CinemaException(
                    "GraphQL query completes successfully but returns no data",
                    request
                )
            }
    }

    override suspend fun executeMutation(query: String, variables: Map<String, Any?>): MutationDto {
        val request = CinemaRequest(query, variables)
        return client.post()
            .uri("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Basic YWRtaW46YWRtaW4=")
            .bodyValue(mapper.writeValueAsString(request))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody!!
            .let { mapper.readValue(it, CinemaMutationResult::class.java) }
            .let { result ->
                result.errors?.takeIf { it.isNotEmpty() }?.let {
                    throw CinemaException("GraphQL query failed", request, it)
                }
                result.data ?: throw CinemaException(
                    "GraphQL query completes successfully but returns no data",
                    request
                )
            }
    }
}