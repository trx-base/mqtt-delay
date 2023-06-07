package integration

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mqtt.MqttConfig
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@MicronautTest
class AcceptanceIntegrationTest : AbstractIntegrationTest() {

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Test
    fun acceptance_00_00() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/5/$delayedTopic",
            MqttMessage("payload".toByteArray()),
        )

        val elapsed = measureTimeMillis {
            messageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(elapsed).isBetween(5000, 6500)
    }

    @Test
    fun acceptance_00_01() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/2/$delayedTopic",
            MqttMessage("payload".toByteArray()),
        )

        val elapsed = measureTimeMillis {
            messageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(elapsed).isBetween(2000, 3000)
    }

    @Test
    fun acceptance_01_00() {
        val messageArrived = expectMessage(delayedTopic)

        mqttClient.publish(
            "${mqttConfig.topic}/5/$delayedTopic",
            MqttMessage(),
        )

        mqttClient.publish(
            "${mqttConfig.topic}/5/$delayedTopic",
            MqttMessage(),
        )

        val elapsed = measureTimeMillis {
            messageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(elapsed).isBetween(5000, 6500)
        assertThat(messageArrived.count).isEqualTo(1)
    }

    @Test
    fun acceptance_02_00() {
        val messageArrived = expectMessage(delayedTopic)

        CoroutineScope(Dispatchers.IO).launch {
            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )
            delay(3000)

            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )

            delay(1000)
            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )
        }

        val elapsed = measureTimeMillis {
            messageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(elapsed).isBetween(5000, 6500)
        Thread.sleep(2000)
        assertThat(messageArrived.count).isEqualTo(1)
    }

    @Test
    fun acceptance_03_00() {
        val messageArrived = expectMessage(delayedTopic)

        CoroutineScope(Dispatchers.IO).launch {
            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )
            delay(1000)

            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )

            delay(6000)
            mqttClient.publish(
                "${mqttConfig.topic}/5/$delayedTopic",
                MqttMessage(),
            )
        }

        val elapsed = measureTimeMillis {
            messageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(elapsed).isBetween(5000, 6000)

        val anotherMessageArrived = expectMessage(delayedTopic)
        val anotherElapsed = measureTimeMillis {
            anotherMessageArrived.get(10, TimeUnit.SECONDS)
        }
        assertThat(anotherElapsed).isBetween(6500, 7500)
    }

    @Test
    fun acceptance_04_00() {
        val messageArrived_topic1 = expectMessage(delayedTopic + "/topic1")
        val messageArrived_topic2 = expectMessage(delayedTopic + "/topic2")

        mqttClient.publish(
            "${mqttConfig.topic}/5/$delayedTopic/topic1",
            MqttMessage("topic1".toByteArray()),
        )
        mqttClient.publish(
            "${mqttConfig.topic}/5/$delayedTopic/topic2",
            MqttMessage("topic2".toByteArray()),
        )

        val elapsed = measureTimeMillis {
            assertThat(messageArrived_topic1.get(10, TimeUnit.SECONDS).payload).isEqualTo("topic1".toByteArray())
            assertThat(messageArrived_topic2.get(10, TimeUnit.SECONDS).payload).isEqualTo("topic2".toByteArray())
        }
        assertThat(elapsed).isBetween(5000, 6500)
    }

    @Test
    fun acceptance_05_00() {
        val messageArrived_topic1 = expectMessage("$delayedTopic/topic1")

        CoroutineScope(Dispatchers.IO).launch {
            mqttClient.publish(
                "${mqttConfig.topic}/reset/5/$delayedTopic/topic1",
                MqttMessage("topic1".toByteArray()),
            )
            delay(2000)

            mqttClient.publish(
                "${mqttConfig.topic}/reset/5/$delayedTopic/topic1",
                MqttMessage("topic1".toByteArray()),
            )
        }

        val elapsed = measureTimeMillis {
            assertThat(messageArrived_topic1.get(10, TimeUnit.SECONDS).payload).isEqualTo("topic1".toByteArray())
        }
        assertThat(elapsed).isBetween(7000, 8000)
    }
}
