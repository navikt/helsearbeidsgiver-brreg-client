package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlin.reflect.KSuspendFunction1

private const val ORG_NR = "123456789"

private val virksomhetMedNavnJson = "virksomhetMedNavn.json".readResource()
private val virksomhetSlettetJson = "virksomhetSlettet.json".readResource()

class BrregClientTest : StringSpec({

    "skal finne virksomhetsnavn" {
        val navn = mockBrregClient(HttpStatusCode.OK, virksomhetMedNavnJson)
            .hentVirksomhetNavn(ORG_NR)

        navn shouldBeEqualComparingTo "Firma AS"
    }

    "skal bruke default navn dersom virksomhet ikke finnes" {
        val navn = mockBrregClient(HttpStatusCode.NotFound)
            .hentVirksomhetNavn(ORG_NR)

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

    alleBrregMetoder(HttpStatusCode.BadRequest)
        .forEach { testFn ->
            "${testFn.name} skal feile ved 4xx-feil utenom 404" {
                shouldThrowExactly<ClientRequestException> {
                    testFn(ORG_NR)
                }
            }
        }

    alleBrregMetoder(HttpStatusCode.InternalServerError)
        .forEach { testFn ->
            "${testFn.name} skal feile ved 5xx-feil" {
                shouldThrowExactly<ServerResponseException> {
                    testFn(ORG_NR)
                }
            }
        }
})

private fun alleBrregMetoder(httpStatus: HttpStatusCode): List<KSuspendFunction1<String, Any>> =
    mockBrregClient(httpStatus)
        .let {
            listOf(
                it::hentVirksomhetNavn,
                it::erVirksomhet
            )
        }

private fun String.readResource(): String =
    ClassLoader.getSystemResource(this).readText()
