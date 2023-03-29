package delay.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import mqtt.MqttClient
import mqtt.MqttConfig
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class SubscriptionServiceTest {

    @InjectMockKs
    var subscriptionService = SubscriptionService()

    @MockK(relaxed = true)
    lateinit var mqttClient: MqttClient

    @MockK(relaxed = true)
    lateinit var delayService: DelayService

    @MockK
    lateinit var mqttConfig: MqttConfig

    @BeforeEach
    fun setUp() {
        every { mqttConfig.topic } returns "delayed-SubscriptionServiceTest"
    }

    @Test
    fun shouldSubscribeToDelayTopics_whenRegisterSubscriptions_andGivenClientId() {
        subscriptionService.registerSubscriptions()
        val topic = mqttConfig.topic
        verify { mqttClient.subscribe("$topic/#", 2, any()) }
        verify { mqttClient.subscribe("+/$topic/#", 2, any()) }
    }

    @Test
    fun shouldProvideDelayPayload_whenMessageArrived() {
        val topic = mqttConfig.topic
        val slot = slot<IMqttMessageListener>()
        every { mqttClient.subscribe("+/$topic/#", 2, capture(slot)) } returns Unit
        subscriptionService.registerSubscriptions()

        val expectedMessage = mockk<MqttMessage>()

        slot.captured.messageArrived(
            "$topic/12/expected/topic",
            expectedMessage,
        )
        verify { delayService.delayMessage(12, "expected/topic", expectedMessage) }
    }

    @Test
    fun shouldExtractDelayPeriod_whenHandleMessage_andGivenPeriodInTopic() {
        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/42/any", mockk())
        verify { delayService.delayMessage(42, "any", any()) }

        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/23/any", mockk())
        verify { delayService.delayMessage(23, "any", any()) }

        subscriptionService.handleDelayedMessage("whatever/${mqttConfig.topic}/1878/any", mockk())
        verify { delayService.delayMessage(1878, "any", any()) }
    }

    @Test
    fun shouldFail_whenHandleMessage_andPeriodNotGivenInTopic() {
        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/", mockk())
        verify(exactly = 0) { delayService.delayMessage(any(), any(), any()) }
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParse_andGivenTopicWithPeriod_125() {
        assertThat(subscriptionService.parseDelayedPeriodAndTopic("root/${mqttConfig.topic}/125/target/topic")).isEqualTo(
            Pair(
                125L,
                "target/topic",
            ),
        )
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParsePeriod_andGivenTopicWithPeriod_666() {
        assertThat(subscriptionService.parseDelayedPeriodAndTopic("root/${mqttConfig.topic}/666/target/topic")).isEqualTo(
            Pair(
                666L,
                "target/topic",
            ),
        )
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParse_andGivenTopicWith_numericRootTopic() {
        assertThat(subscriptionService.parseDelayedPeriodAndTopic("1984/${mqttConfig.topic}/42/target/topic")).isEqualTo(
            Pair(
                42L,
                "target/topic",
            ),
        )
    }

    @Test
    fun shouldThrow_whenParsePeriod_andGivenTopicWith_invalidPeriod() {
        assertThrows<RuntimeException> { subscriptionService.parseDelayedPeriodAndTopic("1984/${mqttConfig.topic}/4notanumber2/target/topic") }
        assertThrows<RuntimeException> { subscriptionService.parseDelayedPeriodAndTopic("1984/${mqttConfig.topic}/nonumber/target/topic") }
        assertThrows<RuntimeException> { subscriptionService.parseDelayedPeriodAndTopic("1984/${mqttConfig.topic}/missing/42/topic") }
    }

    @Test
    fun shouldUseTopicFromConfig_whenParsePeriodAndTopic() {
        val randomDelayedTopic = UUID.randomUUID().toString()
        every { mqttConfig.topic } returns randomDelayedTopic

        assertThat(subscriptionService.parseDelayedPeriodAndTopic("$randomDelayedTopic/1878/another/target")).isEqualTo(
            Pair(1878L, "another/target"),
        )
    }
}
