package no.nav.familie.ba.soknad.api.util

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object TokenBehandler {
    private const val ISSUER = "selvbetjening"

    fun hentToken(): String {
        val contextHolder = SpringTokenValidationContextHolder()
        return contextHolder.tokenValidationContext.getJwtToken(ISSUER).tokenAsString
    }

    fun hentFnr(): String {
        val contextHolder = SpringTokenValidationContextHolder()
        return if (contextHolder.tokenValidationContext.getClaims(ISSUER)["pid"] !== null) {
            contextHolder.tokenValidationContext.getClaims(ISSUER)["pid"].toString()
        }
        else if (contextHolder.tokenValidationContext.getClaims(ISSUER)["sub"] !== null) {
            contextHolder.tokenValidationContext.getClaims(ISSUER)["sub"].toString()
        }
        else {
            error("Finner ikke sub/pid på token")
        }


    }
}
