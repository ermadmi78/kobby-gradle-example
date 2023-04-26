package io.github.ermadmi78.kobby.cinema.server.scalars

import graphql.language.*
import graphql.scalars.util.Kit
import graphql.schema.*
import kotlinx.serialization.json.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

/**
 * Created on 29.03.2023
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
object JsonScalar {
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
                    when (input) {
                        is ObjectValue -> input.extractJsonObject(emptyMap())
                        else -> throw CoercingParseLiteralException("Unexpected literal ${this::class.simpleName}")
                    }

                @Throws(CoercingParseLiteralException::class)
                override fun parseLiteral(input: Any, variables: MutableMap<String, Any>): JsonObject =
                    when (input) {
                        is ObjectValue -> input.extractJsonObject(variables)
                        else -> throw CoercingParseLiteralException("Unexpected literal ${this::class.simpleName}")
                    }

                @Throws(CoercingParseLiteralException::class)
                private fun Value<*>.extractJsonElement(variables: Map<String, Any?>): JsonElement = when (this) {
                    is FloatValue -> JsonPrimitive(value)
                    is StringValue -> JsonPrimitive(value)
                    is IntValue -> JsonPrimitive(value)
                    is BooleanValue -> JsonPrimitive(isValue)
                    is EnumValue -> JsonPrimitive(name)

                    is VariableReference -> try {
                        variables[name].toJsonElement()
                    } catch (e: Exception) {
                        throw CoercingParseLiteralException(e.message, e)
                    }

                    is ArrayValue -> extractJsonArray(variables)
                    is ObjectValue -> extractJsonObject(variables)
                    else -> throw CoercingParseLiteralException("Unexpected literal ${this::class.simpleName}")
                }

                @Throws(CoercingParseLiteralException::class)
                private fun ArrayValue.extractJsonArray(
                    variables: Map<String, Any?>
                ): JsonArray = buildJsonArray {
                    values.forEach {
                        add(it.extractJsonElement(variables))
                    }
                }

                @Throws(CoercingParseLiteralException::class)
                private fun ObjectValue.extractJsonObject(variables: Map<String, Any?>): JsonObject = buildJsonObject {
                    objectFields.forEach {
                        put(it.name, it.value.extractJsonElement(variables))
                    }
                }
            }
        )
        .build()

    @Throws(CoercingSerializeException::class)
    private fun JsonElement.toAny(): Any? = when (this) {
        is JsonPrimitive -> toPrimitive()
        is JsonArray -> toList()
        is JsonObject -> toMap()
    }

    @Throws(CoercingSerializeException::class)
    private fun JsonPrimitive.toPrimitive(): Any? = when (this) {
        is JsonNull -> null
        else -> {
            if (isString) {
                content
            } else {
                when {
                    content == "true" -> true
                    content == "false" -> false

                    content.contains('.') -> try {
                        BigDecimal(content)
                    } catch (e: NumberFormatException) {
                        throw CoercingSerializeException(e.message, e)
                    }

                    else -> try {
                        BigInteger(content)
                    } catch (e: NumberFormatException) {
                        throw CoercingSerializeException(e.message, e)
                    }
                }
            }
        }
    }

    @Throws(CoercingSerializeException::class)
    private fun JsonArray.toList(): List<Any?> = mutableListOf<Any?>().also { list ->
        forEach { element ->
            list += element.toAny()
        }
    }

    @Throws(CoercingSerializeException::class)
    private fun JsonObject.toMap(): Map<String, Any?> = mutableMapOf<String, Any?>().also { map ->
        forEach { (key, element) ->
            map[key] = element.toAny()
        }
    }

    @Throws(CoercingParseValueException::class)
    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is List<*> -> toJsonArray()
        is Map<*, *> -> toJsonObject()
        else -> throw CoercingParseValueException("Unexpected value ${this::class.simpleName}")
    }

    @Throws(CoercingParseValueException::class)
    private fun List<*>.toJsonArray(): JsonArray = buildJsonArray {
        forEach { value ->
            add(value.toJsonElement())
        }
    }

    @Throws(CoercingParseValueException::class)
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