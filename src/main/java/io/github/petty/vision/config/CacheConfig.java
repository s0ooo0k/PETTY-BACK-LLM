package io.github.petty.vision.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // 기본 메모리 캐시: speciesResults, visionResults 두 개의 캐시 영역만 생성
        return new ConcurrentMapCacheManager("speciesResults", "visionResults");
    }
}
