import io.github.ermadmi78.kobby.kobby

description = "todo"

plugins {
    id("io.github.ermadmi78.kobby") version "1.0.0-alpha.03"
}

kobby {
    kotlin {
        scalars = mapOf(
            "DateTime" to typeOf("java.time", "OffsetDateTime"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )
    }
}

val jacksonVersion: String by project
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}