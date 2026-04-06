package jnu.econovation.ecoknockbecentral.grpc.config

import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.AirPurifierServiceGrpc
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.SensorServiceGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddedGrpcClientConfiguration {
    @Bean(destroyMethod = "shutdownNow")
    fun embeddedManagedChannel(config: EmbeddedGrpcConfig): ManagedChannel =
        NettyChannelBuilder
            .forAddress(config.host, config.port)
            .usePlaintext()
            .build()

    @Bean
    fun sensorGrpcStub(channel: ManagedChannel): SensorServiceGrpc.SensorServiceBlockingStub =
        SensorServiceGrpc.newBlockingStub(channel)

    @Bean
    fun airPurifierGrpcStub(channel: ManagedChannel): AirPurifierServiceGrpc.AirPurifierServiceBlockingStub =
        AirPurifierServiceGrpc.newBlockingStub(channel)
}
