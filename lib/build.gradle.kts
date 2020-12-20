import io.kobby.dsl.kobbyDSL

description = "todo"

buildscript {
    dependencies {
        classpath("io.kobby:kobby-gradle-plugin:0.0.0-SNAPSHOT")
    }
}

apply(plugin = "io.kobby")

kobbyDSL {
    source = fileTree("src/main/resources") {
        include("**/*.graphqls")
    }
}
