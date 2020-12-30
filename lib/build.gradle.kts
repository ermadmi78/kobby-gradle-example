import io.kobby.kobby

description = "todo"

buildscript {
    dependencies {
        classpath("io.kobby:kobby-gradle-plugin:0.0.0-SNAPSHOT")
    }
}

apply(plugin = "io.kobby")

kobby {
    schema {
        local = file("src/main/resources/io/kobby/gradle/integration/api/cinema.graphqls")
    }

    schemaSearchTree = fileTree("src/main/resources") {
        include("**/*.graphqls")
    }
    kotlin {
        scalars = mapOf(
            "DateTime" to typeOf("java.time", "OffsetDateTime"),
            "JSON" to typeMap.parameterize(typeString, typeAny.nullable())
        )

        dto {
            jacksonized = false
        }

        api {

        }

        impl {

        }
    }
}
