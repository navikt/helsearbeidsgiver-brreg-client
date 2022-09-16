package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.log.logger

class BrregClient(
    private val url: String
) {
    private val logger = this.logger()

    private val httpClient = createHttpClient()

    suspend fun hentVirksomhetNavn(orgnr: String): String =
        try {
            httpClient.get(url + orgnr)
                .body<UnderenheterResponse>()
                .navn
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

@Serializable
internal data class UnderenheterResponse(
    val navn: String
)
