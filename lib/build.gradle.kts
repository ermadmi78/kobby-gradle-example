import io.github.ermadmi78.kobby.kobby

description = "todo"

buildscript {
    dependencies {
        classpath("io.github.ermadmi78:kobby-gradle-plugin:1.0.0-alpha.03-SNAPSHOT")
    }
}

apply(plugin = "io.github.ermadmi78.kobby")

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