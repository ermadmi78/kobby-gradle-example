import io.kobby.kobby

description = "todo"

buildscript {
    dependencies {
        classpath("io.kobby:kobby-gradle-plugin:0.0.0-SNAPSHOT")
    }
}

apply(plugin = "io.kobby")

kobby {
    kotlin {
        scalars = mapOf(
            "DateTime" to typeOf("java.time", "OffsetDateTime"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )

        //relativePackage = false
        dto {
            jacksonized = false
        }
    }
}
