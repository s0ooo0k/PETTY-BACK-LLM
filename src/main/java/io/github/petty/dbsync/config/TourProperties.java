package io.github.petty.dbsync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "tour-api")
public class TourProperties {
    private Map<String, String> defaultParams; // _type, MobileOS, MobileApp
    private String baseUrl;
    private String serviceKey;
}
