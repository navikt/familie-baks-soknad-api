package no.nav.familie.baks.soknad.api

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class MockOAuth2ServerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        val server: MockOAuth2Server by lazy {
            MockOAuth2Server().also { server ->
                server.start()
                Runtime.getRuntime().addShutdownHook(Thread { server.shutdown() })
            }
        }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val properties =
            mapOf<String, Any>(
                "TOKEN_X_ISSUER" to server.issuerUrl("tokenx").toString(),
                "TOKEN_X_CLIENT_ID" to "aud-localhost",
                "TOKEN_X_JWKS_URI" to server.jwksUrl("tokenx").toString(),
            )

        applicationContext.environment.propertySources.addFirst(
            MapPropertySource("mockOAuth2Server", properties),
        )

        applicationContext.beanFactory.registerSingleton("mockOAuth2Server", server)
    }
}
