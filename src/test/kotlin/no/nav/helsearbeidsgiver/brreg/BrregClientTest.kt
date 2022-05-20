package no.nav.helsearbeidsgiver.brreg

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class BrregClientTest {

    val successResponse = "response.json".loadFromResources()
    val orgNr = "123456789"

    @Test
    fun `Skal finne virksomhetsnavn`() {
        buildClient(successResponse, HttpStatusCode.Accepted).getVirksomhetsNavn(orgNr)
    }

    @Test
    @Disabled
    fun `Skal håndtere feil`() {
        buildClient("", HttpStatusCode.ServiceUnavailable, headersOf(HttpHeaders.ContentType, "plain/text")).getVirksomhetsNavn(orgNr)
    }
}

fun String.loadFromResources(): String {
    return ClassLoader.getSystemResource(this).readText()
}
