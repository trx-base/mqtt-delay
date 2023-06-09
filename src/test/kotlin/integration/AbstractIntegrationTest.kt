package integration

import integration.util.CompletableFutureWithCounter
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Inject
import mqtt.MqttClient
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.BeforeEach
import java.util.*

abstract class AbstractIntegrationTest {

    @Inject
    lateinit var mqttClient: MqttClient

    var delayedTopic = ""

    @BeforeEach
    fun setUp() {
        delayedTopic = "integration-test/target/topic/" + UUID.randomUUID().toString() // Prevents flaky tests.
    }

    fun expectMessage(topic: String): CompletableFutureWithCounter<MqttMessage> {
        val messageArrived = CompletableFutureWithCounter<MqttMessage>()
        mqttClient.subscribe(
            topic,
            0,
        ) { _, message ->
            messageArrived.complete(message)
        }
        return messageArrived
    }
}

@Serdeable
data class DummyObject(val first: String, val second: String)
