package no.nav.familie.ba.soknad.api.util

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object TokenBehandler {
    const val ISSUER = "selvbetjening"

    fun hentToken(): String {
        val contextHolder = SpringTokenValidationContextHolder()
        return contextHolder.tokenValidationContext.getJwtToken(ISSUER).tokenAsString
    }

    fun hentFnr(): String {
        val contextHolder = SpringTokenValidationContextHolder()
        return contextHolder.tokenValidationContext.getClaims(ISSUER)["sub"].toString()
    }
}
