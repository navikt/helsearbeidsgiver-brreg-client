package no.nav.helsearbeidsgiver.brreg

import kotlinx.serialization.Serializable

@Serializable
data class UnderenheterNavnResponse(
    val navn: String
)
