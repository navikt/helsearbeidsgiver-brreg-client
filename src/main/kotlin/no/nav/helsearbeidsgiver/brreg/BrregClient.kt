package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.log.logger

internal const val VIRKSOMHET_NAVN_DEFAULT = "Ukjent arbeidsgiver"

class BrregClient(
    url: String
) {
    private val logger = this.logger()

    private val httpClient = createHttpClient()
    private val correctedUrl = url.trimEnd('/') + "/"

    suspend fun hentVirksomhetNavn(orgnr: String): String? =
        hentVirksomhet(orgnr)
            ?.navn

    suspend fun hentVirksomhetNavnOrDefault(orgnr: String): String =
        hentVirksomhetNavn(orgnr)
            ?: VIRKSOMHET_NAVN_DEFAULT.also {
                logger.error("Fant ikke virksomhet i brreg, bruker default navn '$it'.")
            }

    suspend fun erVirksomhet(orgnr: String): Boolean =
        hentVirksomhet(orgnr)
            ?.let {
                it.slettedato.isNullOrEmpty()
            }
            ?: false

    private suspend fun hentVirksomhet(orgnr: String): Virksomhet? =
        try {
            httpClient.get(correctedUrl + orgnr)
                .body<Virksomhet>()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) null
            else throw e
        }
}

@Serializable
internal data class Virksomhet(
    val navn: String,
    val slettedato: String? = null
)
