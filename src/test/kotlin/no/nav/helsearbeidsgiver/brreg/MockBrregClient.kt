package no.nav.helsearbeidsgiver.brreg

import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.helsearbeidsgiver.utils.test.mock.mockStatic
import kotlin.time.Duration

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun mockBrregClient(vararg responses: Pair<HttpStatusCode, String>): BrregClient {
    val mockEngine = MockEngine.create {
        reuseHandlers = false
        requestHandlers.addAll(
            responses.map { (status, content) ->
                {
                    if (content == "timeout") {
                        // Skrur den virtuelle klokka fremover, nok til at timeout forårsakes
                        dispatcher.shouldNotBeNull().testCoroutineScheduler.advanceTimeBy(1)
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

    val mockHttpClient = HttpClient(mockEngine) {
        customize()

        // Overstyr delay for å unngå at testene bruker lang tid
        install(HttpRequestRetry) {
            customizeRetry()
            constantDelay(
                millis = 1,
                randomizationMs = 1,
            )
        }
    }

    return mockStatic(::createHttpClient) {
        every { createHttpClient() } returns mockHttpClient
        BrregClient(
            url = "url",
            cacheConfig = LocalCache.Config(Duration.ZERO, 1),
        )
    }
}
