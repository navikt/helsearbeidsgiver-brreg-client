package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import no.nav.helsearbeidsgiver.utils.test.resource.readResource

private const val ORG_NR = "123456789"

private val virksomhetMedNavnJson = "virksomhetMedNavn.json".readResource()
private val virksomhetSlettetJson = "virksomhetSlettet.json".readResource()
private val virksomhetIkkeFunnetJson = "ingenTreff.json".readResource()
private val flereTreffJson = "flereTreff.json".readResource()

class BrregClientTest : StringSpec({

    "${BrregClient::hentVirksomhetNavn.name} skal finne virksomhetsnavn" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .hentVirksomhetNavn(ORG_NR)

        navn.shouldNotBeNull()
        navn shouldBeEqualComparingTo "Firma AS"
    }

    "${BrregClient::hentVirksomhetNavn.name} skal gi 'null' dersom vi mottar 'Not found' http-kode" {
        mockBrregClient(HttpStatusCode.NotFound)
            .hentVirksomhetNavn(ORG_NR)
            .shouldBeNull()
    }

    "${BrregClient::hentVirksomhetNavnOrDefault.name} skal finne virksomhetsnavn" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .hentVirksomhetNavnOrDefault(ORG_NR)

        navn shouldBeEqualComparingTo "Firma AS"
    }

    "${BrregClient::hentVirksomhetNavnOrDefault.name} skal gi default navn dersom vi mottar 'Not found' http-kode" {
        val navn = mockBrregClient(HttpStatusCode.NotFound)
            .hentVirksomhetNavnOrDefault(ORG_NR)

        navn shouldBeEqualComparingTo VIRKSOMHET_NAVN_DEFAULT
    }

    "${BrregClient::hentVirksomhetNavn.name} skal gi default navn dersom virksomhet ikke finnes" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetIkkeFunnetJson)
            .hentVirksomhetNavnOrDefault(ORG_NR)
        navn shouldBeEqualComparingTo VIRKSOMHET_NAVN_DEFAULT
    }

    "${BrregClient::hentVirksomhetNavn.name} skal gi 'null' dersom virksomhet ikke finnes" {
        mockBrregClient(HttpStatusCode.OK, virksomhetIkkeFunnetJson)
            .hentVirksomhetNavn(ORG_NR)
            .shouldBeNull()
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

    "hent flere virksomheter" {
        val parametere = listOf("123456789", "012345678", "987654321")
        val virksomheter = mockBrregClient(HttpStatusCode.OK, flereTreffJson).hentVirksomheter(parametere)
        parametere.forEachIndexed { i, parameter ->
            parameter shouldBe virksomheter[i]?.organisasjonsnummer
            "Firma $i" shouldBe virksomheter[i]?.navn
        }
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
