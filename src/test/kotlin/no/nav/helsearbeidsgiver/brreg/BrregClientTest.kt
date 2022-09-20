package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

const val ORG_NR = "123456789"

class BrregClientTest : StringSpec({

    "skal finne virksomhetsnavn" {
        val successResponse = "response.json".readResource()

        val navn = mockBrregClient(successResponse, HttpStatusCode.Accepted)
            .hentVirksomhetNavn(ORG_NR)

        navn shouldBeEqualComparingTo "Firma AS"
    }

    "skal bruke default navn dersom organisasjon ikke finnes" {
        val navn = mockBrregClient("", HttpStatusCode.NotFound)
            .hentVirksomhetNavn(ORG_NR)

        navn shouldBeEqualComparingTo "Arbeidsgiver"
    }

    "skal feile ved 4xx-feil utenom 404" {
        shouldThrowExactly<ClientRequestException> {
            mockBrregClient("", HttpStatusCode.BadRequest)
                .hentVirksomhetNavn(ORG_NR)
        }
    }
})

private fun String.readResource(): String =
    ClassLoader.getSystemResource(this).readText()
