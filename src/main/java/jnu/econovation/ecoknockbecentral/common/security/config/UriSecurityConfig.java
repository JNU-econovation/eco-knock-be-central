package jnu.econovation.ecoknockbecentral.common.security.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.uri")
public record UriSecurityConfig(
        List<String> allowedFrontEndOrigins,
        OAuth2 oAuth2
) {

    public record OAuth2(
            String successPath,
            String defaultRedirectOrigin
    ) {

    }
}

