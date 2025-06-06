package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestRetryConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import no.nav.helsearbeidsgiver.utils.json.jsonConfig

internal fun createHttpClient(): HttpClient =
    HttpClient(Apache5) { customize() }

internal fun HttpClientConfig<*>.customize() {
    expectSuccess = true

    install(ContentNegotiation) {
        json(jsonConfig)
    }

    install(HttpRequestRetry) { customizeRetry() }

    install(HttpTimeout) {
        connectTimeoutMillis = 500
        requestTimeoutMillis = 500
        socketTimeoutMillis = 500
    }
}

internal fun HttpRequestRetryConfig.customizeRetry() {
    retryOnException(
        maxRetries = 5,
        retryOnTimeout = true,
    )
    constantDelay(
        millis = 500,
        randomizationMs = 500,
    )
}
