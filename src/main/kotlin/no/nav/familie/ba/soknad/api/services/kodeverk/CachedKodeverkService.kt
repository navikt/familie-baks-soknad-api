package no.nav.familie.ba.soknad.api.services.kodeverk

import no.nav.familie.ba.soknad.api.clients.kodeverk.KodeverkClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedKodeverkService(private val kodeverkClient: KodeverkClient) {

    @Cacheable("kodeverk_postested")
    fun hentPostnummer(): Map<String, String> = kodeverkClient.hentPostnummer().mapTerm()
}
