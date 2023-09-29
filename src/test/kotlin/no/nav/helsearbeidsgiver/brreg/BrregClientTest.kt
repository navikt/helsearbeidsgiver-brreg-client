package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import no.nav.helsearbeidsgiver.utils.test.resource.readResource

private const val ORG_NR = "123456789"

private val virksomhetMedNavnJson = "virksomhetMedNavn.json".readResource()
private val virksomhetSlettetJson = "virksomhetSlettet.json".readResource()

class BrregClientTest : StringSpec({

    "${BrregClient::hentVirksomhetNavn.name} skal finne virksomhetsnavn" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .hentVirksomhetNavn(ORG_NR)

        navn.shouldNotBeNull()
        navn shouldBeEqualComparingTo "Firma AS"
    }

    "${BrregClient::hentVirksomhetNavn.name} skal gi 'null' dersom virksomhet ikke finnes" {
        mockBrregClient(HttpStatusCode.NotFound)
            .hentVirksomhetNavn(ORG_NR)
            .shouldBeNull()
    }

    "${BrregClient::hentVirksomhetNavnOrDefault.name} skal finne virksomhetsnavn" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .hentVirksomhetNavnOrDefault(ORG_NR)

        navn shouldBeEqualComparingTo "Firma AS"
    }

    "${BrregClient::hentVirksomhetNavnOrDefault.name} skal gi default navn dersom virksomhet ikke finnes" {
        val navn = mockBrregClient(HttpStatusCode.NotFound)
            .hentVirksomhetNavnOrDefault(ORG_NR)

        navn shouldBeEqualComparingTo VIRKSOMHET_NAVN_DEFAULT
    }

    "funnet virksomhet uten slettedato bekrefter eksistens" {
        mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .erVirksomhet(ORG_NR)
            .shouldBeTrue()
    }

    "funnet virksomhet med slettedato avkrefter eksistens" {
        mockBrregClient(HttpStatusCode.OK, virksomhetSlettetJson)
            .erVirksomhet(ORG_NR)
            .shouldBeFalse()
    }

    "ikke funnet virksomhet avkrefter eksistens" {
        mockBrregClient(HttpStatusCode.NotFound)
            .erVirksomhet(ORG_NR)
            .shouldBeFalse()
    }

    listOf(
        BrregClient::hentVirksomhetNavn,
        BrregClient::hentVirksomhetNavnOrDefault,
        BrregClient::erVirksomhet,
    )
        .forEach { testFn ->
            "${testFn.name} skal feile ved 4xx-feil utenom 404" {
                shouldThrowExactly<ClientRequestException> {
                    testFn(
                        mockBrregClient(HttpStatusCode.BadRequest),
                        ORG_NR,
                    )
                }
            }

            "${testFn.name} skal feile ved 5xx-feil" {
                shouldThrowExactly<ServerResponseException> {
                    testFn(
                        mockBrregClient(HttpStatusCode.InternalServerError),
                        ORG_NR,
                    )
                }
            }
        }
})
