package no.nav.familie.ba.soknad.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@Suppress("ArrayInDataClass")
@ConfigurationProperties(prefix = "cors")
@ConstructorBinding
internal data class CorsProperties(val allowedOrigins: Array<String>)