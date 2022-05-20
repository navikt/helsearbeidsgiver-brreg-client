package no.nav.helsearbeidsgiver.norg2

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

fun buildHttpClientJson(status: HttpStatusCode, response: Any): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            // serializer = buildJacksonSerializer()
            expectSuccess = false
        }
//        engine {
//            addHandler {
//                respond(
//                    buildObjectMapper().writeValueAsString(response),
//                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
//                    status = status
//                )
//            }
//        }
    }
}
