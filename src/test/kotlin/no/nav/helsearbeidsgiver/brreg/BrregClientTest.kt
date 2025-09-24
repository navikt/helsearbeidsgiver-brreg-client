package no.nav.helsearbeidsgiver.brreg

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import no.nav.helsearbeidsgiver.utils.test.resource.readResource
import no.nav.helsearbeidsgiver.utils.wrapper.Orgnr

private val ORGNR_1 = "115232541"
private val ORGNR_2 = "609959703"
private val ORGNR_3 = "530940357"
private val ORGNR_SLETTET = "419741485"

private val orgMedNavnJson = "orgMedNavn.json".readResource()
private val orgSlettetJson = "orgSlettet.json".readResource()

class BrregClientTest :
    FunSpec({

        context(BrregClient::hentOrganisasjonNavn.name) {
            test("henter organisasjonsnavn") {
                val navnByOrgnr =
                    mockBrregClient(HttpStatusCode.OK to orgMedNavnJson)
                        .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))

                navnByOrgnr shouldContainExactly
                    mapOf(
                        Orgnr(ORGNR_1) to "Kopper og krus AS",
                        Orgnr(ORGNR_2) to "Boller og brus AS",
                        Orgnr(ORGNR_3) to "Gråstein og grus AS",
                    )
            }

            // Skal ikke få 404 ved bruk av nytt kall, men beholder foreløpig
            test("gir 'null' ved HTTP-status '404 Not Found'") {
                mockBrregClient(HttpStatusCode.NotFound to "")
                    .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))
                    .shouldBeEmpty()
            }

            test("gir 'null' dersom organisasjon ikke er tilstede i respons") {
                mockBrregClient(HttpStatusCode.OK to "{}")
                    .hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3))
                    .shouldBeEmpty()
            }
        }

        context(BrregClient::erOrganisasjon.name) {
            test("organisasjon uten slettedato bekrefter eksistens") {
                mockBrregClient(HttpStatusCode.OK to orgMedNavnJson)
                    .erOrganisasjon(ORGNR_3)
                    .shouldBeTrue()
            }

            test("organisasjon med slettedato avkrefter eksistens") {
                mockBrregClient(HttpStatusCode.OK to orgSlettetJson)
                    .erOrganisasjon(ORGNR_SLETTET)
                    .shouldBeFalse()
            }

            // Skal ikke få 404 ved bruk av nytt kall, men beholder foreløpig
            test("organisasjon avkreftes eksistens ved HTTP-status '404 Not Found'") {
                mockBrregClient(HttpStatusCode.NotFound to "")
                    .erOrganisasjon(ORGNR_3)
                    .shouldBeFalse()
            }
        }

        listOf<Pair<String, suspend BrregClient.() -> Unit>>(
            BrregClient::hentOrganisasjonNavn.name to { hentOrganisasjonNavn(setOf(ORGNR_1, ORGNR_2, ORGNR_3)) },
            BrregClient::erOrganisasjon.name to { erOrganisasjon(ORGNR_3) },
        ).forEach { (testFnName, testFn) ->
            context(testFnName) {
                test("feiler ved 4xx-feil utenom 404") {
                    val mockBrregClient = mockBrregClient(HttpStatusCode.BadRequest to "")

                    val e =
                        shouldThrowExactly<ClientRequestException> {
                            mockBrregClient.testFn()
                        }

                    e.response.status shouldBe HttpStatusCode.BadRequest
                }

                test("lykkes ved færre 5xx-feil enn max retries (5)") {
                    val mockBrregClient =
                        mockBrregClient(
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.OK to orgMedNavnJson,
                        )

                    runTest {
                        shouldNotThrowAny {
                            mockBrregClient.testFn()
                        }
                    }
                }

                test("feiler ved flere 5xx-feil enn max retries (5)") {
                    val mockBrregClient =
                        mockBrregClient(
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                            HttpStatusCode.InternalServerError to "",
                        )

                    runTest {
                        val e =
                            shouldThrowExactly<ServerResponseException> {
                                mockBrregClient.testFn()
                            }

                        e.response.status shouldBe HttpStatusCode.InternalServerError
                    }
                }

                test("kall feiler og prøver på nytt ved timeout") {
                    val mockBrregClient =
                        mockBrregClient(
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to "timeout",
                            HttpStatusCode.OK to orgMedNavnJson,
                        )

                    runTest {
                        shouldNotThrowAny {
                            mockBrregClient.testFn()
                        }
                    }
                }
            }
        }
    })
