package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import no.nav.helsearbeidsgiver.utils.test.resource.readResource
import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Orgnr

private val ORGNR_1 = Orgnr("115232541")
private val ORGNR_2 = Orgnr("609959703")
private val ORGNR_3 = Orgnr("530940357")
private val ORGNR_SLETTET = Orgnr("419741485")

private val orgMedNavnJson = "orgMedNavn.json".readResource()
private val orgSlettetJson = "orgSlettet.json".readResource()

class BrregClientTest : FunSpec({

    context(BrregClient::hentOrganisasjonNavn.name) {
        test("henter organisasjonsnavn") {
            val navnByOrgnr = mockBrregClient(HttpStatusCode.OK, orgMedNavnJson)
                .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))

            navnByOrgnr shouldContainExactly mapOf(
                ORGNR_1 to "Kopper og krus AS",
                ORGNR_2 to "Boller og brus AS",
                ORGNR_3 to "Gråstein og grus AS",
            )
        }

        // Skal ikke få 404 ved bruk av nytt kall, men beholder foreløpig
        test("gir 'null' ved HTTP-status '404 Not Found'") {
            mockBrregClient(HttpStatusCode.NotFound)
                .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))
                .shouldBeEmpty()
        }

        test("gir 'null' dersom organisasjon ikke er tilstede i respons") {
            mockBrregClient(HttpStatusCode.OK, "{}")
                .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))
                .shouldBeEmpty()
        }
    }

    context(BrregClient::erOrganisasjon.name) {
        test("organisasjon uten slettedato bekrefter eksistens") {
            mockBrregClient(HttpStatusCode.OK, orgMedNavnJson)
                .erOrganisasjon(ORGNR_3)
                .shouldBeTrue()
        }

        test("organisasjon med slettedato avkrefter eksistens") {
            mockBrregClient(HttpStatusCode.OK, orgSlettetJson)
                .erOrganisasjon(ORGNR_SLETTET)
                .shouldBeFalse()
        }

        // Skal ikke få 404 ved bruk av nytt kall, men beholder foreløpig
        test("organisasjon avkreftes eksistens ved HTTP-status '404 Not Found'") {
            mockBrregClient(HttpStatusCode.NotFound)
                .erOrganisasjon(ORGNR_3)
                .shouldBeFalse()
        }
    }

    listOf<Pair<String, suspend BrregClient.() -> Unit>>(
        BrregClient::hentOrganisasjonNavn.name to { hentOrganisasjonNavn(setOf(Orgnr.genererGyldig(), Orgnr.genererGyldig())) },
        BrregClient::erOrganisasjon.name to { erOrganisasjon(Orgnr.genererGyldig()) },
    )
        .forEach { (testFnName, testFn) ->
            context(testFnName) {
                test("skal feile ved 4xx-feil utenom 404") {
                    shouldThrowExactly<ClientRequestException> {
                        mockBrregClient(HttpStatusCode.BadRequest).testFn()
                    }
                }

                test("skal feile ved 5xx-feil") {
                    shouldThrowExactly<ServerResponseException> {
                        mockBrregClient(HttpStatusCode.InternalServerError).testFn()
                    }
                }
            }
        }
})
