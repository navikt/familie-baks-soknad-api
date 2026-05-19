package no.nav.familie.baks.soknad.api

import no.nav.security.mock.oauth2.MockOAuth2Server
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
}
