package no.nav.familie.baks.soknad.api.config

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.jsonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @param:Value("\${TOKEN_X_ISSUER}") private val tokenXIssuer: String,
    @param:Value("\${TOKEN_X_CLIENT_ID}") private val tokenXClientId: String,
    @param:Value("\${TOKEN_X_JWKS_URI}") private val tokenXJwksUri: String
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt { jwtDecoder = jwtDecoder() }
            }
            exceptionHandling {
                authenticationEntryPoint =
                    AuthenticationEntryPoint { _, response, authException ->
                        response.status = HttpStatus.UNAUTHORIZED.value()
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        response.writer.write(
                            jsonMapper.writeValueAsString(Ressurs.failure<String>(authException.message ?: "Ikke autentisert"))
                        )
                    }
            }
        }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(tokenXJwksUri).build()
        decoder.setJwtValidator(
            createDefaultWithValidators(
                JwtIssuerValidator(tokenXIssuer),
                JwtAudienceValidator(tokenXClientId),
                JwtClaimValidator<String>("acr") { acr -> acr == LEVEL4 || acr == IDPORTEN_LOA_HIGH }
            )
        )
        return decoder
    }

    companion object {
        const val LEVEL4 = "Level4"
        const val IDPORTEN_LOA_HIGH = "idporten-loa-high"
    }
}
