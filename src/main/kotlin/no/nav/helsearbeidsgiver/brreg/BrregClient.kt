package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.utils.log.logger

class BrregClient(
    private val brregUrl: String,
    private val httpClient: HttpClient
) {
    private val logger = this.logger()

    fun getVirksomhetsNavn(orgnr: String): String =
        try {
            runBlocking {
                httpClient.get(brregUrl + orgnr) {
                    expectSuccess = true
                }
                    .body<UnderenheterResponse>()
                    .navn
            }
                .also { logger.info("Fant virksomheten.") }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                "Arbeidsgiver"
                    .also { logger.error("Fant ikke virksomhet i brreg, bruker default navn '$it'.") }
            } else {
                logger.error("Klarte ikke å hente virksomhet!", e)
                throw e
            }
        }
}
