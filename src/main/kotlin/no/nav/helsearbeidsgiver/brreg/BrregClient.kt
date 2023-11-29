package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.log.logger

internal const val VIRKSOMHET_NAVN_DEFAULT = "Ukjent arbeidsgiver"

class BrregClient(
    url: String,
) {
    private val logger = this.logger()

    private val httpClient = createHttpClient()
    private val correctedUrl = url.trimEnd('/') + "?size=100&"

    suspend fun hentVirksomheter(orgnr: List<String>): List<Virksomhet?> {
        val parameter = orgnr.tilParameter()
        return hentVirksomhet(parameter)
    }

    suspend fun hentVirksomhetNavn(orgnr: String): String? =
        hentVirksomhet(orgnr).firstOrNull()
            ?.navn

    suspend fun hentVirksomhetNavnOrDefault(orgnr: String): String =
        hentVirksomhetNavn(orgnr)
            ?: VIRKSOMHET_NAVN_DEFAULT.also {
                logger.error("Fant ikke virksomhet i brreg, bruker default navn '$it'.")
            }

    suspend fun erVirksomhet(orgnr: String): Boolean =
        hentVirksomhet(orgnr)
            .let {
                return it.isNotEmpty() && it.firstOrNull()?.slettedato.isNullOrEmpty()
            }

    private fun <T> List<T>.tilParameter(): String {
        return joinToString(separator = ",")
    }

    private suspend fun hentVirksomhet(orgnr: String): List<Virksomhet?> =
        try {
            httpClient.get(correctedUrl + "organisasjonsnummer=$orgnr")
                .body<Payload>()._embedded?.underenheter.orEmpty()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                emptyList()
            } else {
                throw e
            }
        }
}

@Serializable
internal data class Payload(
    val _embedded: VirksomhetListe? = null,
)

@Serializable
internal data class VirksomhetListe(
    val underenheter: List<Virksomhet>? = null,
)

@Serializable
data class Virksomhet(
    val navn: String,
    val organisasjonsnummer: String,
    val slettedato: String? = null,
)
