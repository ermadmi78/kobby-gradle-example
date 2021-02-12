import io.kobby.kobby

description = "todo"

buildscript {
    dependencies {
        classpath("io.kobby:kobby-gradle-plugin:1.0.0-alpha02-SNAPSHOT")
    }
}

apply(plugin = "io.kobby")

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