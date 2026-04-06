package jnu.econovation.ecoknockbecentral.grpc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "embedded.grpc")
data class EmbeddedGrpcConfig(
    val host: String,
    val port: Int,
    val timeout: Duration,
)
