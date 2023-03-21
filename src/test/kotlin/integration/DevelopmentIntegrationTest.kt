package integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import delay.mqtt.MqttConfig
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import util.CompletableFutureWithCounter
import java.util.concurrent.TimeUnit

@MicronautTest
class DevelopmentIntegrationTest : AbstractIntegrationTest() {

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Test
    fun shouldPublishDelayedMessage_whenPublishToDelayed_givenStringAsPayload() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/0/$delayedTopic",
            MqttMessage("Expected delayed message payload.".toByteArray()),
        )

        val delayedPayload = String(messageArrived.get(1, TimeUnit.SECONDS).payload)
        assertThat(delayedPayload).isEqualTo("Expected delayed message payload.")
    }

    @Test
    fun shouldPublishDelayedMessage_whenPublishToDelayed_givenObjectAsPayload() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/0/$delayedTopic",
            MqttMessage(ObjectMapper.getDefault().writeValueAsBytes(DummyObject("first", "second"))),
        )

        val delayedPayload = ObjectMapper.getDefault()
            .readValue(messageArrived.get(1, TimeUnit.SECONDS).payload, DummyObject::class.java)
        assertThat(delayedPayload).isEqualTo(DummyObject("first", "second"))
    }

    @Test
    fun shouldPublishSecondDelayedMessage_whenFirstMessageFailed_givenInvalidDelayedMessage() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/1failure/$delayedTopic",
            MqttMessage("First message".toByteArray()),
        )

        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic",
            MqttMessage("The second message.".toByteArray()),
        )

        val delayedMessage = messageArrived.get(2, TimeUnit.SECONDS)
        assertThat(String(delayedMessage.payload)).isEqualTo("The second message.")
    }

    @Test
    fun shouldPublishOnlyOnce_whenDelayedAgainToAlreadyDelayedTopic() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic",
            MqttMessage("payload 1".toByteArray()),
        )
        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic",
            MqttMessage("payload 2".toByteArray()),
        )

        val delayedMessage = messageArrived.get(2, TimeUnit.SECONDS)
        assertThat(String(delayedMessage.payload)).isEqualTo("payload 1")
        assertThat(messageArrived.count).isEqualTo(1)
    }

    @Test
    fun shouldPublishAll_whenDelayedDifferentTopics() {
        val messageArrived1 = expectMessage("$delayedTopic/first")
        val messageArrived2 = expectMessage("$delayedTopic/second")
        val messageArrived3 = expectMessage("$delayedTopic/third")

        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic/first",
            MqttMessage("payload 1".toByteArray()),
        )
        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic/second",
            MqttMessage("payload 2".toByteArray()),
        )
        mqttClient.publish(
            "${mqttConfig.topic}/1/$delayedTopic/third",
            MqttMessage("payload 3".toByteArray()),
        )

        assertThat(messageArrived1.get(2, TimeUnit.SECONDS).payload).isEqualTo("payload 1".toByteArray())
        assertThat(messageArrived1.count).isEqualTo(1)

        assertThat(messageArrived2.get().payload).isEqualTo("payload 2".toByteArray())
        assertThat(messageArrived2.count).isEqualTo(1)

        assertThat(messageArrived3.get().payload).isEqualTo("payload 3".toByteArray())
        assertThat(messageArrived3.count).isEqualTo(1)
    }

    @Test
    fun shouldUseNewFormatForDelay() {
        val delayedTopic = "please/delay/me"
        val messageArrived = CompletableFutureWithCounter<String>()
        mqttClient.subscribe(
            delayedTopic,
            0,
        ) { _, message ->
            messageArrived.complete(String(message.payload))
        }

        mqttClient.publish(
            "${mqttConfig.topic}/0/$delayedTopic",
            MqttMessage("Hello, new delay world".toByteArray()),
        )

        assertThat(messageArrived.get(1, TimeUnit.SECONDS)).isEqualTo("Hello, new delay world")
    }
}
