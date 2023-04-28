package delay.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import delay.model.DelayRequest
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
        verify { delayService.delayMessage(DelayRequest(12, "expected/topic", false, expectedMessage)) }
    }

    @Test
    fun shouldExtractDelayPeriod_whenHandleMessage_andGivenPeriodInTopic() {
        val mqttMessage = mockk<MqttMessage>()

        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/42/any", mqttMessage)
        verify { delayService.delayMessage(DelayRequest(42, "any", false, mqttMessage)) }

        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/23/any", mqttMessage)
        verify { delayService.delayMessage(DelayRequest(23, "any", false, mqttMessage)) }

        subscriptionService.handleDelayedMessage("whatever/${mqttConfig.topic}/1878/any", mqttMessage)
        verify { delayService.delayMessage(DelayRequest(1878, "any", false, mqttMessage)) }
    }

    @Test
    fun shouldFail_whenHandleMessage_andPeriodNotGivenInTopic() {
        subscriptionService.handleDelayedMessage("${mqttConfig.topic}/", mockk())
        verify(exactly = 0) { delayService.delayMessage(any()) }
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParse_andGivenTopicWithPeriod_125() {
        val mqttMessage = mockk<MqttMessage>()
        assertThat(
            subscriptionService.parseMqttTopic(
                "root/${mqttConfig.topic}/125/target/topic",
                mqttMessage,
            ),
        ).isEqualTo(
            DelayRequest(
                125L,
                "target/topic",
                false,
                mqttMessage,
            ),
        )
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParsePeriod_andGivenTopicWithPeriod_666() {
        val mqttMessage = mockk<MqttMessage>()
        assertThat(
            subscriptionService.parseMqttTopic(
                "root/${mqttConfig.topic}/666/target/topic",
                mqttMessage,
            ),
        ).isEqualTo(
            DelayRequest(
                666L,
                "target/topic",
                false,
                mqttMessage,
            ),
        )
    }

    @Test
    fun shouldReturnPeriodAndTopic_whenParse_andGivenTopicWith_numericRootTopic() {
        val mqttMessage = mockk<MqttMessage>()
        assertThat(
            subscriptionService.parseMqttTopic(
                "1984/${mqttConfig.topic}/42/target/topic",
                mqttMessage,
            ),
        ).isEqualTo(
            DelayRequest(42, "target/topic", false, mqttMessage),

        )
    }

    @Test
    fun shouldThrow_whenParsePeriod_andGivenTopicWith_invalidPeriod() {
        assertThrows<RuntimeException> {
            subscriptionService.parseMqttTopic(
                "1984/${mqttConfig.topic}/4notanumber2/target/topic",
                mockk(),
            )
        }
        assertThrows<RuntimeException> {
            subscriptionService.parseMqttTopic(
                "1984/${mqttConfig.topic}/nonumber/target/topic",
                mockk(),
            )
        }
        assertThrows<RuntimeException> {
            subscriptionService.parseMqttTopic(
                "1984/${mqttConfig.topic}/missing/42/topic",
                mockk(),
            )
        }
    }

    @Test
    fun shouldUseTopicFromConfig_whenParsePeriodAndTopic() {
        val randomDelayedTopic = UUID.randomUUID().toString()
        every { mqttConfig.topic } returns randomDelayedTopic

        val mqttMessage = mockk<MqttMessage>()
        assertThat(
            subscriptionService.parseMqttTopic(
                "$randomDelayedTopic/1878/another/target",
                mqttMessage,
            ),
        ).isEqualTo(
            DelayRequest(1878L, "another/target", false, mqttMessage),
        )
    }

    @Test
    fun shouldReturnResetFlag_whenParse_andGivenTopicWith_reset() {
        val mqttMessage = mockk<MqttMessage>()
        assertThat(
            subscriptionService.parseMqttTopic(
                "1984/${mqttConfig.topic}/reset/42/target/topic",
                mqttMessage,
            ),
        ).isEqualTo(
            DelayRequest(
                42L,
                "target/topic",
                true,
                mqttMessage,
            ),
        )
    }
}
