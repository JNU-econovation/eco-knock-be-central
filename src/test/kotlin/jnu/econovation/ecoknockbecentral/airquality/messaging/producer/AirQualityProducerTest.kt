package jnu.econovation.ecoknockbecentral.airquality.messaging.producer

import io.micrometer.core.instrument.Timer
import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawSensorDTO
import jnu.econovation.ecoknockbecentral.airquality.queue.AutoControlAirPurifierQueue
import jnu.econovation.ecoknockbecentral.airquality.queue.SaveAirQualityQueue
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import jnu.econovation.ecoknockbecentral.grpc.client.airpurifier.AirPurifierGrpcClient
import jnu.econovation.ecoknockbecentral.grpc.client.sensor.SensorGrpcClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class AirQualityProducerTest {
    private val sensorGrpcClient = mock<SensorGrpcClient>()
    private val airPurifierGrpcClient = mock<AirPurifierGrpcClient>()
    private val metrics = mock<ApplicationMetrics>()
    private val saveQueue = SaveAirQualityQueue(metrics)
    private val autoControlQueue = AutoControlAirPurifierQueue(metrics)

    @Test
    fun `publishes current AQI as pm25 when average AQI differs`() = runBlocking {
        whenever(metrics.startTimer()).thenReturn(mock<Timer.Sample>())
        whenever(sensorGrpcClient.getCurrentSensor()).thenReturn(sensor())
        whenever(airPurifierGrpcClient.getCurrentAirPurifier()).thenReturn(airPurifier(aqi = 32, averageAqi = 8))
        val producer = AirQualityProducer(
            sensorGrpcClient = sensorGrpcClient,
            airPurifierGrpcClient = airPurifierGrpcClient,
            saveQueue = saveQueue,
            autoControlQueue = autoControlQueue,
            metrics = metrics,
        )

        producer.start()

        try {
            val command: SaveAirQualityCommand = withTimeout(5.seconds) {
                saveQueue.asFlow().first()
            }

            assertThat(command.airQuality.pm25).isEqualTo(32)
            Unit
        } finally {
            producer.cancel()
        }
    }

    private fun sensor() = RawSensorDTO(
        temperatureC = 24.0,
        humidityRh = 45.0,
        gasResistanceOhm = 100.0,
        status = 0,
        gasValid = true,
        heatStable = true,
        measuredAt = Instant.now(),
        staticIaq = 20.0,
        estimatedEco2PPM = 500.0,
        estimatedBvocPPM = 0.5,
        accuracy = 3,
        stabilizationProgressPercent = 100,
        gasPercentage = 0.0,
        learningCompleteAt = Instant.now(),
    )

    private fun airPurifier(aqi: Int, averageAqi: Int) = RawAirPurifierDTO(
        power = "on",
        isOn = true,
        aqi = aqi,
        averageAqi = averageAqi,
        humidity = 40,
        temperatureC = 23.0,
        mode = "auto",
        favoriteLevel = 10,
        filterLifeRemaining = 100,
        filterHoursUsed = 0,
        motorSpeed = 1,
        purifyVolume = 0,
        led = true,
        ledBrightness = null,
        buzzer = null,
        childLock = false,
        measuredAt = Instant.now(),
    )
}
