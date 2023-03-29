package delay.service

import assertk.assertThat
import assertk.assertions.isBetween
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import mqtt.MqttClient
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.system.measureTimeMillis

@ExtendWith(MockKExtension::class)
class DelayServiceTest {

    @InjectMockKs
    var delayService: DelayService = DelayService()

    @MockK(relaxed = true)
    lateinit var mqttClient: MqttClient

    @MockK(relaxed = true)
    lateinit var storageService: StorageService

    @Test
    fun shouldPublishToDelayedTopic_whenGivenTopicAndMessage() {
        val expectedMessage = mockk<MqttMessage>()

        delayService.delayMessage(0, "topic/to/delay", expectedMessage)
        verify(timeout = 1000) {
            mqttClient.publish(
                "topic/to/delay",
                expectedMessage,
            )
        }
    }

    @Test
    fun shouldDelayPublish_whenGivenDelayPeriod() {
        delayService.delayMessage(2, "topic/to/delay", mockk())

        val elapsed = measureTimeMillis {
            verify(timeout = 3000) { mqttClient.publish(any(), any()) }
        }
        assertThat(elapsed).isBetween(2000, 3000)
    }

    @Test
    fun shouldPublishDelayedTopic_whenTopic_notMarkedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns null
        delayService.delayMessage(1, "topic/to/delay", mockk())
        verify(timeout = 2000) { mqttClient.publish(any(), any()) }
    }

    @Test
    fun shouldNotPublishDelayedTopic_whenTopic_markedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns "true"
        delayService.delayMessage(2, "topic/to/delay", mockk())
        verify(timeout = 1000, exactly = 0) { mqttClient.publish(any(), any()) }
    }

    @Test
    fun shouldMarkTopicAsDelayed_whenDelayedMessage() {
        delayService.delayMessage(2, "topic/to/delay", mockk())
        verify { storageService.put("topic/to/delay", "true") }
    }

    @Test
    fun shouldNotMarkTopicAsDelayed_whenDelayedMessage_alreadyMarkedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns "true"
        delayService.delayMessage(2, "topic/to/delay", mockk())
        verify(exactly = 0) { storageService.put("topic/to/delay", "true") }
    }

    @Test
    fun shouldMarkTopicAsDelayed_beforeDelayCountdownStarts() {
        delayService.delayMessage(2, "topic/to/delay", mockk())
        verify(timeout = 500) { storageService.put("topic/to/delay", "true") }
    }

    @Test
    fun shouldUnmarkTopicAsDelayed_afterPublish() {
        delayService.delayMessage(0, "topic/to/delay", mockk())
        verifyOrder {
            storageService.put(any(), any())
            mqttClient.publish(any(), any())
            storageService.remove("topic/to/delay")
        }
    }
}
