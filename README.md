# Example of Kotlin Spring Boot GraphQL Server with Spring Security authentication and authorization along with Kotlin GraphQL DSL Client generated by means of [Kobby Gradle Plugin](https://github.com/ermadmi78/kobby) from GraphQL Schema

This example uses [Kotlinx Serialization](https://github.com/ermadmi78/kobby/issues/7) engine. To see an example with Jackson serialization switch to branch [jackson](https://github.com/ermadmi78/kobby-gradle-example/tree/jackson). 

1. See GraphQL schema [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/resources/io/github/ermadmi78/kobby/cinema/api/cinema.graphqls)
1. Start server: `./gradlew :cinema-server:bootRun`
1. Try to execute GraphQL queries in [console](http://localhost:8080/graphiql) with login/password `admin/admin` (for example `query { countries { id name } }`)
1. Start client: `./gradlew :cinema-kotlin-client:bootRun`
1. See queries, generated by Kobby DSL in client output
1. See client source code [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-kotlin-client/src/main/kotlin/io/github/ermadmi78/kobby/cinema/kotlin/client/application.kt)
1. Just try to write your own query by means of Kobby DSL!

See Maven example [here](https://github.com/ermadmi78/kobby-maven-example)

# Spring Security authorization support

See example of GraphQL controller authorization [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-server/src/main/kotlin/io/github/ermadmi78/kobby/cinema/server/controller/QueryController.kt)

See example of GraphQL server API tests [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-server/src/test/kotlin/io/github/ermadmi78/kobby/cinema/server/CinemaServerTest.kt)

See example of GraphQL subscription tests [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-server/src/test/kotlin/io/github/ermadmi78/kobby/cinema/server/CinemaSubscriptionsTest.kt) 

**GraphQL query authorization example:**

```kotlin
suspend fun country(id: Long): CountryDto? = hasAnyRole("USER", "ADMIN") {
    println("Query country by user [${authentication.name}] in thread [${Thread.currentThread().name}]")
    dslContext.selectFrom(COUNTRY)
        .where(COUNTRY.ID.eq(id))
        .fetchAny { it.toDto() }
}
```

**GraphQL mutation authorization example:**

```kotlin
override suspend fun createCountry(name: String): CountryDto = hasAnyRole("ADMIN") {
    println(
        "Mutation create country by user [${authentication.name}] " +
                "in thread [${Thread.currentThread().name}]"
    )
    val newCountry = dslContext.insertInto(COUNTRY)
        .set(COUNTRY.NAME, name)
        .returning()
        .fetchOne()!!
        .toDto()

    eventBus.fireCountryCreated(newCountry)
    newCountry
}
```

**GraphQL subscription authorization example:**

```kotlin
override suspend fun countryCreated(): Publisher<CountryDto> = hasAnyRole("USER", "ADMIN") {
    println(
        "Subscription on country created by user [${authentication.name}] " +
                "in thread [${Thread.currentThread().name}]"
    )
    eventBus.countryCreatedFlow().asPublisher()
}
```

# Kotlin GraphQL Client DSL support

GraphQL Client DSL in this example is generated by means of [Kobby Gradle Plugin](https://github.com/ermadmi78/kobby) from [GraphQL Schema](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/resources/io/github/ermadmi78/kobby/cinema/api/cinema.graphqls).
See source code [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-kotlin-client/src/main/kotlin/io/github/ermadmi78/kobby/cinema/kotlin/client/application.kt).

You can see more details about the generated GraphQL DSL
[here](https://github.com/ermadmi78/kobby/wiki/Overview-of-generated-GraphQL-DSL)

You can see more details about the Kobby customization directives 
(`@default`, `@required`, `@primaryKey` and `@selection`) 
[here](https://github.com/ermadmi78/kobby/wiki/Client-DSL-Customization)

## Simple GraphQL query example

**GraphQL query:**

```graphql
{
  country(id: 1) {
    id
    name
  }
}
```

**Kotlin DSL query:**

```kotlin
context.query {
    country(1) {
        // id is primary key (see @primaryKey directive in schema)
        // name is default (see @default directive in schema)
    }
}
```

## Complex GraphQL query example

**GraphQL query:**

```graphql
{
  country(id: 7) {
    id
    name
    films(title: "d") {
      id
      title
      genre
      countryId
      actors(limit: -1) {
        id
        firstName
        lastName
        birthday
        gender
        countryId
        country {
          id
          name
        }
      }
    }
    actors(firstName: "d") {
      id
      fields(keys: ["birthday", "gender"])
      firstName
      lastName
      birthday
      gender
      countryId
      films(limit: -1) {
        id
        title
        countryId
      }
    }
  }
}
```

**Kotlin DSL query:**

```kotlin
context.query {
    country(7) {
        // id is primary key
        // name is default
        films {
            title = "d" // title is selection argument (see @selection directive in schema)

            // id is primary key
            // title is default
            genre()
            // countryId is required (see @required directive in schema)
            actors {
                limit = -1 // limit is selection argument (see @selection directive in schema)

                // id is primary key
                // firstName is default
                // lastName is default
                // birthday is required (see @required directive in schema)
                gender()
                // countryId is primary key
                country {
                    // id is primary key
                    // name is default
                }
            }
        }
        actors {
            firstName = "d" // firstName is selection argument (see @selection directive in schema)

            // id is primary key
            fields {
                keys = listOf(
                    "birthday",
                    "gender"
                ) // keys is selection argument (see @selection directive in schema)
            }
            // firstName is default
            // lastName is default
            // birthday is required
            gender()
            // countryId is primary key
            films {
                limit = -1 // limit is selection argument (see @selection directive in schema)

                // id is primary key
                // title is default
                // countryId is required
            }
        }
    }
}
```

## GraphQL unions query example

**GraphQL query:**

```graphql
{
  country(id: 17) {
    id
    native {
      __typename
      ... on Film {
        id
        title
        genre
        countryId
      }
      ... on Actor {
        id
        firstName
        lastName
        birthday
        gender
        countryId
        country {
          id
          name
        }
      }
    }
  }
}
```

**Kotlin DSL query:**

```kotlin
context.query {
    country(17) {
        __minimize() // switch off defaults to minimize query
        // id is primary key
        native {
            // __typename generated by Kobby
            __onFilm {
                // id is primary key
                // title is default
                genre()
                // countryId is required
            }
            __onActor {
                // id is primary key
                // firstName is default
                // lastName is default
                // birthday is required
                gender()
                // countryId is primary key
                country {
                    // id is primary key
                    // name is default
                }
            }
        }
    }
}
```

## GraphQL subscription example

**GraphQL subscription:**

```graphql
subscription {
  filmCreated(countryId: 1) {
    id
    title
    countryId
    country {
      id
      name
    }
  }
}
```

```kotlin
context.subscription {
    filmCreated(countryId = 1) {
        // id is primary key (see @primaryKey directive in schema)
        // title is default (see @default directive in schema)
        // countryId is required (see @required directive in schema)
        country {
            // id is primary key (see @primaryKey directive in schema)
            // name is default (see @default directive in schema)
        }
    }
}.subscribe {
    while (true) {
        val newFilm = receive().filmCreated
        println("<< Film created: id=${newFilm.id} name=${newFilm.title} country=${newFilm.country.name}")
    }
}
```

## API Customization

You can customize the generated GraphQL DSL by means of
Kotlin [extension functions](https://kotlinlang.org/docs/extensions.html). 
Note that all generated entities contains `__context()` function that returns instance of DSL Context interface. 
So each entity contains an entry point for executing GraphQL queries.

**First, let extend our DSL Context (source code
see [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/kotlin/io/github/ermadmi78/kobby/cinema/api/kobby/kotlin/_CinemaContext.kt)):**

```kotlin
suspend fun CinemaContext.findCountry(
    id: Long, 
    __projection: CountryProjection.() -> Unit = {}
): Country? =
    query {
        country(id, __projection)
    }.country

suspend fun CinemaContext.fetchCountry(
    id: Long, 
    __projection: CountryProjection.() -> Unit = {}
): Country = 
    findCountry(id, __projection)!!
```

**Second, let extend our Country entity (source code
see [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/kotlin/io/github/ermadmi78/kobby/cinema/api/kobby/kotlin/entity/_Country.kt)):**

```kotlin
suspend fun Country.refresh(
    __projection: (CountryProjection.() -> Unit)? = null
): Country =
    __context().query {
        country(id) {
            __projection?.invoke(this) ?: __withCurrentProjection()
        }
    }.country!!

suspend fun Country.findFilms(
    __query: CountryFilmsQuery.() -> Unit = {}
): List<Film> =
    refresh {
        __minimize() // switch off all default fields to minimize GraphQL response
        films(__query)
    }.films
```

**Ok, we are ready to use our customized API:**

```kotlin
 // Fetch country by id
val country = context.fetchCountry(7)
println("Country: id=${country.id} name='${country.name}'")

//Find all country films
val films = country.findFilms {
    limit = -1
    genre()
}

films.forEach {
    println("Film: id=${it.id}, title='${it.title}' genre=${it.genre}")
}
```

**This Kotlin DSL code will produce two GraphQL queries:**

```graphql
{
  country(id: 7) {
    id
    name
  }
}
```

```graphql
{
  country(id: 7) {
    id
    films(limit: -1) {
      id
      title
      genre
      countryId
    }
  }
}
```

**More sophisticated example of API customization see
in [API tests](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-server/src/test/kotlin/io/github/ermadmi78/kobby/cinema/server/CinemaServerTest.kt)
.**

The tests `createCountryWithFilmAndActorsByMeansOfGeneratedAPI`
and `createCountryWithFilmAndActorsByMeansOfCustomizedAPI`
implements the same scenario by means of native generated API and by means of customized API. You can compare these two
test cases to see that the customized API significantly improves the readability of your code.

### Subscription API customization

You can customize subscriptions DSL too.

**First, let extend our DSL Context (source code
see [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/kotlin/io/github/ermadmi78/kobby/cinema/api/kobby/kotlin/_CinemaContext.kt)):**

```kotlin
fun CinemaContext.onFilmCreated(
    countryId: Long?,
    __projection: FilmProjection.() -> Unit = {}
): CinemaSubscriber<Film> = CinemaSubscriber {
    subscription {
        filmCreated(countryId, __projection)
    }.subscribe {
        it(CinemaReceiver {
            receive().filmCreated
        })
    }
}
```

**Second, extend our Country entity (source code
see [here](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-api/src/main/kotlin/io/github/ermadmi78/kobby/cinema/api/kobby/kotlin/entity/_Country.kt)):**

```kotlin
fun Country.onFilmCreated(__projection: FilmProjection.() -> Unit = {}): CinemaSubscriber<Film> =
    __context().onFilmCreated(id, __projection)
```

**Ok, we are ready to listen new films in country:**
```kotlin
val australia = context.fetchCountry(1)
australia.onFilmCreated().subscribe {
    while (true) {
        val newFilm = receive()
        println("<< Film created: id=${newFilm.id} " +
                "name=${newFilm.title}")
    }
}
```

**More examples of subscription customization see in 
[Subscription Tests](https://github.com/ermadmi78/kobby-gradle-example/blob/main/cinema-server/src/test/kotlin/io/github/ermadmi78/kobby/cinema/server/CinemaSubscriptionsTest.kt)
.**

The tests `subscriptionsByMeansOfGeneratedAPI` and `subscriptionsByMeansOfCustomizedAPI`
implements the same scenario by means of native generated subscription API and by means of customized API.
