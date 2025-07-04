package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import kotlinx.coroutines.delay
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.helsearbeidsgiver.utils.test.mock.mockStatic
import kotlin.time.Duration

fun mockBrregClient(vararg responses: Pair<HttpStatusCode, String>): BrregClient {
    val mockEngine = MockEngine.create {
        reuseHandlers = false
        requestHandlers.addAll(
            responses.map { (status, content) ->
                {
                    if (content == "timeout") {
                        delay(600)
                    }
                    respond(
                        content = content,
                        status = status,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            },
        )
    }

    val mockHttpClient = HttpClient(mockEngine) { configure() }

    return mockStatic(::createHttpClient) {
        every { createHttpClient() } returns mockHttpClient
        BrregClient(
            url = "url",
            cacheConfig = LocalCache.Config(Duration.ZERO, 1),
        )
    }
}
