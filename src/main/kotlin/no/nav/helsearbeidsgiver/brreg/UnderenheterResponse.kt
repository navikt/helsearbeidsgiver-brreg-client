package no.nav.helsearbeidsgiver.brreg

import kotlinx.serialization.Serializable

@Serializable
data class UnderenheterResponse(
    val navn: String
)
