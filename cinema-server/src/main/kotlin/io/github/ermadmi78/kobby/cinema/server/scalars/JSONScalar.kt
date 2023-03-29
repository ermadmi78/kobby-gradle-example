package io.github.ermadmi78.kobby.cinema.server.scalars

import graphql.scalars.util.Kit
import graphql.schema.*
import kotlinx.serialization.json.*
import java.util.*

/**
 * Created on 29.03.2023
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
object JSONScalar {
    val INSTANCE: GraphQLScalarType = GraphQLScalarType.newScalar()
        .name("JSON")
        .description("A universally unique identifier compliant JsonObject Scalar")
        .coercing(
            object : Coercing<JsonObject, Map<String, Any?>> {
                @Throws(CoercingSerializeException::class)
                override fun serialize(input: Any): Map<String, Any?> = when (input) {
                    is JsonObject -> input.toMap()
                    else -> throw CoercingSerializeException(
                        "Expected a 'JsonObject' type but was '${Kit.typeName(input)}'"
                    )
                }

                @Throws(CoercingParseValueException::class)
                override fun parseValue(input: Any): JsonObject = when (input) {
                    is JsonObject -> input
                    is Map<*, *> -> input.toJsonObject()
                    else -> throw CoercingParseValueException(
                        "Expected a 'JsonObject' or 'Map<*, *>' type but was '${Kit.typeName(input)}'"
                    )
                }

                @Throws(CoercingParseLiteralException::class)
                override fun parseLiteral(input: Any): JsonObject =
                    throw CoercingParseLiteralException("Ignore")
            }
        )
        .build()

    private fun JsonElement.toAny(): Any? = when (this) {
        is JsonPrimitive -> toPrimitive()
        is JsonArray -> toList()
        is JsonObject -> toMap()
    }

    private fun JsonPrimitive.toPrimitive(): Any? = when (this) {
        is JsonNull -> null
        else -> {
            if (isString) {
                content
            } else {
                when {
                    content == "true" -> true
                    content == "false" -> false
                    content.contains('.') -> content.toDouble()
                    else -> content.toLong()
                }
            }
        }
    }

    private fun JsonArray.toList(): List<Any?> = mutableListOf<Any?>().also { list ->
        forEach { element ->
            list += element.toAny()
        }
    }

    private fun JsonObject.toMap(): Map<String, Any?> = mutableMapOf<String, Any?>().also { map ->
        forEach { (key, element) ->
            map[key] = element.toAny()
        }
    }

    @Throws(CoercingSerializeException::class)
    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is List<*> -> toJsonArray()
        is Map<*, *> -> toJsonObject()
        else -> throw CoercingParseValueException("Unexpected value ${this::class.simpleName}")
    }

    @Throws(CoercingSerializeException::class)
    private fun List<*>.toJsonArray(): JsonArray = buildJsonArray {
        forEach { value ->
            add(value.toJsonElement())
        }
    }

    @Throws(CoercingSerializeException::class)
    private fun Map<*, *>.toJsonObject(): JsonObject = buildJsonObject {
        forEach { (key, value) ->
            put(
                (key as? String) ?: throw CoercingParseValueException(
                    "Unexpected key: ${key?.let { it::class.simpleName } ?: "null"}"
                ), value.toJsonElement()
            )
        }
    }
}