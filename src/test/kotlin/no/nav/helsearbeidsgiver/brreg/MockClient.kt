package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun mockClient(
    response: String,
    status: HttpStatusCode = HttpStatusCode.OK
): BrregClient {
    val mockEngine = MockEngine {
        respond(
            content = response,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    val httpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    return BrregClient("", httpClient)
}
