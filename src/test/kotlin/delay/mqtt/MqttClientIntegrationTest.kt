package delay.mqtt

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@MicronautTest
class MqttClientIntegrationTest {

    @Inject
    lateinit var mqttClient: MqttClient

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Test
    fun shouldHaveConnectedClient_whenConnect() {
        assertThat(mqttClient.mqttAsyncClient.isConnected).isTrue()
    }

    @Test
    fun shouldUseConfig_whenConnectingMqtt() {
        assertThat(mqttClient.mqttAsyncClient.serverURI).isEqualTo("tcp://public.trxbroker.org:1883")
        assertThat(mqttClient.mqttAsyncClient.clientId).startsWith("mqtt-delay")
    }

    @Test
    fun shouldReceiveMessage_whenSubscribeThenPublish() {
        val topic = "${mqttConfig.topic}/shouldReceiveMessage_whenSubscribeThenPublish"
        val expectedCall = CompletableFuture<String>()
        mqttClient.subscribe(
            topic,
            0,
        ) { _, message ->
            expectedCall.complete("$message")
        }
        mqttClient.publish(topic, MqttMessage("expectedMessage".toByteArray()))
        assertThat(
            expectedCall.get(
                1,
                TimeUnit.SECONDS,
            ),
        ).isEqualTo("expectedMessage")
    }
}
