package jnu.econovation.ecoknockbecentral.grpc.runner

import io.grpc.Server
import io.grpc.ServerBuilder
import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.grpc.config.GrpcServerConfig
import jnu.econovation.ecoknockbecentral.grpc.health.GrpcHealthService
import jnu.econovation.ecoknockbecentral.grpc.sensor.SensorGrpcService
import mu.KLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GrpcServerRunner(
    private val grpcHealthService: GrpcHealthService,
    private val sensorGrpcService: SensorGrpcService,
    private val config: GrpcServerConfig,
) : KLogging() {
    private var grpcServer: Server? = null

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        grpcServer?.let { return }

        grpcServer = ServerBuilder
            .forPort(config.port)
            .addService(grpcHealthService.bindableService())
            .addService(sensorGrpcService)
            .build()
            .start()

        grpcHealthService.markServing()
        logger.info { "gRPC server started on port ${config.port}" }
    }

    @PreDestroy
    fun stop() {
        val server = grpcServer ?: return

        grpcHealthService.markNotServing()
        server.shutdown()
        if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
            server.shutdownNow()
        }

        logger.info { "gRPC server stopped" }
    }
}
