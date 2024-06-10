package no.nav.familie.baks.soknad.api.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    @Primary
    fun cacheManager(): CacheManager =
        object : ConcurrentMapCacheManager() {
            override fun createConcurrentMapCache(name: String): Cache {
                val concurrentMap =
                    Caffeine
                        .newBuilder()
                        .initialCapacity(100)
                        .maximumSize(1000)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build<Any, Any>()
                        .asMap()
                return ConcurrentMapCache(name, concurrentMap, true)
            }
        }

    @Bean("dailyCache")
    fun dailyCache(): CacheManager =
        object : ConcurrentMapCacheManager() {
            override fun createConcurrentMapCache(name: String): Cache {
                val concurrentMap =
                    Caffeine
                        .newBuilder()
                        .maximumSize(10500)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .recordStats()
                        .build<Any, Any>()
                        .asMap()
                return ConcurrentMapCache(name, concurrentMap, true)
            }
        }
}
