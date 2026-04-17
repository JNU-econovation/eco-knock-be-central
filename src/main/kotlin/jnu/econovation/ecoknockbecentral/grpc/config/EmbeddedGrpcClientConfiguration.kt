package jnu.econovation.ecoknockbecentral.grpc.config

import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.AirPurifierServiceGrpc
import jnu.econovation.ecoknockbecentral.grpc.sensor.v2.SensorServiceGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddedGrpcClientConfiguration {
    @Bean(destroyMethod = "shutdownNow")
    fun embeddedManagedChannel(config: EmbeddedGrpcConfig): ManagedChannel {
        return NettyChannelBuilder
            .forAddress(config.host, config.port)
            .usePlaintext()
            .build()
    }

    @Bean
    fun sensorGrpcStub(channel: ManagedChannel): SensorServiceGrpc.SensorServiceBlockingV2Stub {
        return SensorServiceGrpc.newBlockingV2Stub(channel)
    }

    @Bean
    fun airPurifierGrpcStub(channel: ManagedChannel): AirPurifierServiceGrpc.AirPurifierServiceBlockingStub {
        return AirPurifierServiceGrpc.newBlockingStub(channel)
    }
}
