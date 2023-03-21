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

    @Test
    fun shouldHaveConnectedClient_whenConnect() {
        assertThat(mqttClient.instance.isConnected).isTrue()
    }

    @Test
    fun shouldUseConfig_whenConnectingMqtt() {
        assertThat(mqttClient.instance.serverURI).isEqualTo("tcp://public.trxbroker.org:1883")
        assertThat(mqttClient.instance.clientId).startsWith("delay_test")
    }

    @Test
    fun shouldReceiveMessage_whenSubscribeThenPublish() {
        val topic = "shouldReceiveMessage_whenSubscribeThenPublish"
        val expectedCall = CompletableFuture<String>()
        mqttClient.subscribe(
            topic,
            0,
        ) { sTopic, message ->
            expectedCall.complete("$sTopic - $message")
        }
        mqttClient.publish(topic, MqttMessage("expectedMessage".toByteArray()))
        assertThat(
            expectedCall.get(
                1,
                TimeUnit.SECONDS,
            ),
        ).isEqualTo("shouldReceiveMessage_whenSubscribeThenPublish - expectedMessage")
    }
}
