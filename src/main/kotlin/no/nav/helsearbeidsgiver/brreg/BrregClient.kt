package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.helsearbeidsgiver.utils.wrapper.Orgnr

class BrregClient(
    url: String,
    cacheConfig: LocalCache.Config,
) {
    private val correctedUrl = url.trimEnd('/') + "?size=100&"
    private val httpClient = createHttpClient()
    private val cache = LocalCache<Organisasjon>(cacheConfig)

    suspend fun hentOrganisasjonNavn(orgnr: Set<String>): Map<Orgnr, String> =
        hentOrganisasjoner(orgnr)
            .mapKeys { Orgnr(it.key) }
            .mapValues { it.value.navn }

    suspend fun erOrganisasjon(orgnr: String): Boolean =
        hentOrganisasjoner(setOf(orgnr))
            .values
            .firstOrNull()
            // Kompilatoren mener at 'let' er redundant. Den lyver.
            ?.let { it.slettedato.isNullOrEmpty() }
            ?: false

    private suspend fun hentOrganisasjoner(orgnr: Set<String>): Map<String, Organisasjon> =
        cache.getOrPut(orgnr) {
            val organisasjoner =
                try {
                    val orgnrKommaSeparert = orgnr.joinToString(separator = ",")
                    httpClient
                        .get(correctedUrl + "organisasjonsnummer=$orgnrKommaSeparert")
                        .body<Respons>()
                        ._embedded
                        ?.underenheter
                        .orEmpty()
                } catch (e: ClientRequestException) {
                    if (e.response.status == HttpStatusCode.NotFound) {
                        emptyList()
                    } else {
                        throw e
                    }
                }

            organisasjoner.associateBy { it.organisasjonsnummer }
        }
}

@Serializable
private data class Respons(
    val _embedded: Organisasjoner? = null,
)

@Serializable
private data class Organisasjoner(
    val underenheter: List<Organisasjon>? = null,
)

@Serializable
private data class Organisasjon(
    val navn: String,
    val organisasjonsnummer: String,
    val slettedato: String? = null,
)
