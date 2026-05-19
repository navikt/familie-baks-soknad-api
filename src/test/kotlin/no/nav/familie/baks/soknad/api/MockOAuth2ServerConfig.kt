package no.nav.familie.baks.soknad.api

import no.nav.familie.baks.soknad.api.config.SecurityConfig
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

abstract class MockOAuth2ServerConfig {
    companion object {
        val mockOAuth2Server: MockOAuth2Server by lazy {
            MockOAuth2Server().also { server ->
                server.start()
                Runtime.getRuntime().addShutdownHook(Thread { server.shutdown() })
            }
        }

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun mockOAuth2ServerProperties(registry: DynamicPropertyRegistry) {
            registry.add("TOKEN_X_ISSUER") { mockOAuth2Server.issuerUrl("tokenx").toString() }
            registry.add("TOKEN_X_CLIENT_ID") { "aud-localhost" }
            registry.add("TOKEN_X_JWKS_URI") { mockOAuth2Server.jwksUrl("tokenx").toString() }
            registry.add("TOKEN_X_WELL_KNOWN_URL") { mockOAuth2Server.wellKnownUrl("tokenx").toString() }
        }
    }

    fun lagTestToken(
        issuerId: String = "tokenx",
        audience: String = "aud-localhost",
        claims: Map<String, Any> = mapOf("acr" to SecurityConfig.IDPORTEN_LOA_HIGH),
        expiry: Long = 3600,
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId = issuerId,
                clientId = "test-client",
                tokenCallback =
                    DefaultOAuth2TokenCallback(
                        issuerId = issuerId,
                        subject = "12345678901",
                        audience = listOf(audience),
                        claims = claims,
                        expiry = expiry,
                    ),
            ).serialize()
}
