package io.github.petty.dbsync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;


/**
 * WebClient 관련 설정을 위한 클래스
 */
@Configuration
@EnableConfigurationProperties(TourProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient webClient(TourProperties properties) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(properties.getBaseUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
