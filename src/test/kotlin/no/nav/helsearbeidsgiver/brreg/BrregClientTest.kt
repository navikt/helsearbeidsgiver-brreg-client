package no.nav.helsearbeidsgiver.brreg

import io.ktor.http.HttpStatusCode
import no.nav.helsearbeidsgiver.norg2.buildHttpClientJson
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BrregClientTest {

    @Test
    fun `Skal finne virksomhetsnavn`() {
        BrregClient(buildHttpClientJson(HttpStatusCode.Accepted, ""), "").getVirksomhetsNavn("abd")
    }

    @Test
    fun `Skal håndtere feil`() {
        BrregClient(buildHttpClientJson(HttpStatusCode.InternalServerError, ""), "").getVirksomhetsNavn("abd")
    }
}
