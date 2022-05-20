package no.nav.helsearbeidsgiver.brreg

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class BrregClient(private val httpClient: HttpClient, private val brregUrl: String) {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    fun getVirksomhetsNavn(orgnr: String): String {
        return try {
            val (navn) = runBlocking {
                httpClient.get<UnderenheterNavnResponse>(brregUrl + orgnr)
            }
            log.info("Fant virksomheten")
            navn
        } catch (cause2: ServerResponseException) {
            throw cause2
        } catch (cause: ClientRequestException) {
            if (404 == cause.response.status.value) {
                log.error("Fant ikke virksomhet i brreg")
                "Arbeidsgiver"
            } else {
                log.error("Klarte ikke å hente virksomhet!", cause)
                throw cause
            }
        }
    }
}
