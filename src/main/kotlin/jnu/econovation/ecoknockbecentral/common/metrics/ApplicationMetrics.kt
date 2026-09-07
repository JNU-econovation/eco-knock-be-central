package jnu.econovation.ecoknockbecentral.common.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class ApplicationMetrics(
    private val meterRegistry: MeterRegistry,
) {
    private companion object {
        const val SUCCESS = "success"
        const val FAILURE = "failure"
    }

    fun startTimer(): Timer.Sample {
        return Timer.start(meterRegistry)
    }

    fun stopPollingCycle(sample: Timer.Sample, pipeline: String, outcome: String) {
        stop(sample, "eco.knock.polling.cycle", "pipeline", pipeline, "outcome", outcome)
    }

    fun incrementQueueEnqueue(queue: String) {
        meterRegistry.counter("eco.knock.queue.enqueue", "queue", queue).increment()
    }

    fun <T> recordGrpcClient(client: String, operation: String, action: () -> T): T {
        return record("eco.knock.grpc.client", "client", client, "operation", operation, action)
    }

    suspend fun <T> recordSuspendingGrpcClient(
        client: String,
        operation: String,
        action: suspend () -> T,
    ): T {
        val sample = startTimer()
        return try {
            action().also {
                stop(sample, "eco.knock.grpc.client", "client", client, "operation", operation, "outcome", SUCCESS)
            }
        } catch (throwable: Throwable) {
            stop(sample, "eco.knock.grpc.client", "client", client, "operation", operation, "outcome", FAILURE)
            throw throwable
        }
    }

    fun <T> recordQueueProcessing(queue: String, action: () -> T): T {
        return record("eco.knock.queue.processing", "queue", queue, action)
    }

    fun <T> recordAutoControlAction(decision: String, action: () -> T): T {
        return record("eco.knock.auto.control.action", "decision", decision, action)
    }

    fun <T> recordAirQualityAggregation(resolution: String, action: () -> T): T {
        return record("eco.knock.air.quality.aggregation", "resolution", resolution, action)
    }

    private fun <T> record(metricName: String, firstTag: String, firstValue: String, action: () -> T): T {
        val sample = startTimer()
        return try {
            action().also {
                stop(sample, metricName, firstTag, firstValue, "outcome", SUCCESS)
            }
        } catch (throwable: Throwable) {
            stop(sample, metricName, firstTag, firstValue, "outcome", FAILURE)
            throw throwable
        }
    }

    private fun <T> record(
        metricName: String,
        firstTag: String,
        firstValue: String,
        secondTag: String,
        secondValue: String,
        action: () -> T,
    ): T {
        val sample = startTimer()
        return try {
            action().also {
                stop(sample, metricName, firstTag, firstValue, secondTag, secondValue, "outcome", SUCCESS)
            }
        } catch (throwable: Throwable) {
            stop(sample, metricName, firstTag, firstValue, secondTag, secondValue, "outcome", FAILURE)
            throw throwable
        }
    }

    private fun stop(sample: Timer.Sample, metricName: String, vararg tags: String) {
        sample.stop(Timer.builder(metricName).tags(*tags).register(meterRegistry))
    }

}
