package jnu.econovation.ecoknockbecentral.grpc.sensor

import io.grpc.Status
import io.grpc.stub.StreamObserver
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.ReportSensorRequest
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.ReportSensorResponse
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.SensorServiceGrpc
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.reportSensorResponse
import jnu.econovation.ecoknockbecentral.sensor.dto.internal.SensorReportDTO
import jnu.econovation.ecoknockbecentral.sensor.handler.SensorReportHandler
import org.springframework.stereotype.Component

@Component
class SensorGrpcService(
    private val sensorReportHandler: SensorReportHandler,
) : SensorServiceGrpc.SensorServiceImplBase() {

    override fun reportSensor(
        request: ReportSensorRequest,
        responseObserver: StreamObserver<ReportSensorResponse>,
    ) {
        if (request.deviceId <= 0) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("device_id must be greater than 0")
                    .asRuntimeException(),
            )
            return
        }

        val report = SensorReportDTO.from(request)

        val accepted = sensorReportHandler.handle(report)

        responseObserver.onNext(
            reportSensorResponse {
                this.accepted = accepted
            },
        )
        responseObserver.onCompleted()
    }
}
