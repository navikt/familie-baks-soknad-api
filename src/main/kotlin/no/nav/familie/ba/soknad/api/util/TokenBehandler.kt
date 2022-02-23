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
        val claims = contextHolder.tokenValidationContext.getClaims(ISSUER)
        return claims.getStringClaim("pid")
            ?: claims.getStringClaim("sub")
            ?: error("Finner ikke sub/pid på token")
    }
}
