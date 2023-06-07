package delay.service

import assertk.assertThat
import assertk.assertions.isBetween
import delay.model.DelayRequest
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import mqtt.MqttClient
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.system.measureTimeMillis

@ExtendWith(MockKExtension::class)
class DelayServiceTest {

    @InjectMockKs
    var delayService: DelayService = DelayService()

    @MockK(relaxed = true)
    lateinit var mqttClient: MqttClient

    @MockK
    lateinit var storageService: StorageService

    @BeforeEach
    fun setUp() {
        every { storageService.get(any()) } returns null
        every { storageService.put(any(), any()) } returns Unit
        every { storageService.remove(any()) } returns Unit
    }

    @Test
    fun shouldPublishToDelayedTopic_whenGivenTopicAndMessage() {
        val expectedMessage = mockk<MqttMessage>()

        delayService.delayMessage(DelayRequest(0, "topic/to/delay", false, expectedMessage))
        verify(timeout = 1000) {
            mqttClient.publish(
                "topic/to/delay",
                expectedMessage,
            )
        }
    }

    @Test
    fun shouldDelayPublish_whenGivenDelayPeriod() {
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))

        val elapsed = measureTimeMillis {
            verify(timeout = 3000) { mqttClient.publish(any(), any()) }
        }
        assertThat(elapsed).isBetween(2000, 3000)
    }

    @Test
    fun shouldPublishDelayedTopic_whenTopic_notMarkedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns null
        delayService.delayMessage(DelayRequest(1, "topic/to/delay", false, mockk()))
        verify(timeout = 2000) { mqttClient.publish(any(), any()) }
    }

    @Test
    fun shouldNotPublishDelayedTopic_whenTopic_markedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns mockk()
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))
        verify(timeout = 1000, exactly = 0) { mqttClient.publish(any(), any()) }
    }

    @Test
    fun shouldMarkTopicAsDelayed_whenDelayedMessage() {
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))
        verify { storageService.put("topic/to/delay", any()) }
    }

    @Test
    fun shouldNotMarkTopicAsDelayed_whenDelayedMessage_alreadyMarkedAsDelayed() {
        every { storageService.get("topic/to/delay") } returns mockk()
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))
        verify(exactly = 0) { storageService.put("topic/to/delay", any()) }
    }

    @Test
    fun shouldMarkTopicAsDelayed_beforeDelayCountdownStarts() {
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))
        verify(timeout = 500) { storageService.put("topic/to/delay", any()) }
    }

    @Test
    fun shouldUnmarkTopicAsDelayed_afterPublish() {
        delayService.delayMessage(DelayRequest(0, "topic/to/delay", false, mockk()))
        verifyOrder {
            storageService.put(any(), any())
            mqttClient.publish(any(), any())
            storageService.remove("topic/to/delay")
        }
    }

    @Test
    fun shouldDelay_whenReset_andGivenNotNullInStorage() {
        val coroutineScopeMock = mockk<CoroutineScope>(relaxed = true)
        every { storageService.get("topic/to/delay") } returns coroutineScopeMock
        every { coroutineScopeMock.cancel() } returns Unit
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", true, mockk()))
        val elapsed = measureTimeMillis {
            verify(timeout = 3000) { mqttClient.publish(any(), any()) }
        }
        assertThat(elapsed).isBetween(2000, 3000)
    }

    @Test
    fun shouldNotDelay_whenNotReset_andGivenNullInStorage() {
        every { storageService.get("topic/to/delay") } returns null
        delayService.delayMessage(DelayRequest(2, "topic/to/delay", false, mockk()))
        val elapsed = measureTimeMillis {
            verify(timeout = 3000) { mqttClient.publish(any(), any()) }
        }
        assertThat(elapsed).isBetween(2000, 3000)
    }
}
