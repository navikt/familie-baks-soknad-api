package no.nav.familie.baks.soknad.api.services.kodeverk

import no.nav.familie.baks.soknad.api.clients.kodeverk.KodeverkClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedKodeverkService(
    private val kodeverkClient: KodeverkClient
) {
    @Cacheable("kodeverk_alle_land", cacheManager = "dailyCache")
    fun hentAlleLand(): Map<String, String> = kodeverkClient.hentAlleLand().mapTerm()

    @Cacheable("kodeverk_eos_land", cacheManager = "dailyCache")
    fun hentEØSLand(): Map<String, String> = kodeverkClient.hentEØSLand().mapTerm()

    @Cacheable("kodeverk_postested", cacheManager = "dailyCache")
    fun hentPostnummer(): Map<String, String> = kodeverkClient.hentPostnummer().mapTerm()
}
