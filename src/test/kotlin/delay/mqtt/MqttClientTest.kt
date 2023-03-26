package delay.mqtt

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MqttClientTest {

    @InjectMockKs
    @SpyK
    var mqttClient = MqttClient()

    @RelaxedMockK
    lateinit var iMqttAsyncClient: IMqttAsyncClient

    @RelaxedMockK
    lateinit var mqttConfig: MqttConfig

    @Test
    fun shouldUseMqttMessage_whenPublish() {
        val mqttMessage = mockk<MqttMessage>()
        mqttClient.publish("someTopic", mqttMessage)
        verify { iMqttAsyncClient.publish(any(), eq(mqttMessage)) }
    }

    @Test
    fun shouldSetExpectedConnectionOptions_whenConnectMqttClient() {
        val slot = slot<MqttConnectionOptions>()
        every { iMqttAsyncClient.connect(capture(slot)) } returns mockk(relaxed = true)

        mqttClient.connect()

        assertThat(slot.captured.isCleanStart).isTrue()
        assertThat(slot.captured.isAutomaticReconnect).isTrue()
    }

    @Test
    fun shouldConnect_whenPostconstruct() {
        mqttClient.postConstruct()
        verify { mqttClient.connect() }
    }

    @Test
    fun shouldNotSetUserNameAndPassword_whenNotGivenInConfig() {
        every { mqttConfig.username } returns null
        every { mqttConfig.password } returns null

        val slot = slot<MqttConnectionOptions>()
        every { iMqttAsyncClient.connect(capture(slot)) } returns mockk(relaxed = true)

        mqttClient.connect()

        assertThat(slot.captured.userName).isNull()
        assertThat(slot.captured.password).isNull()
    }

    @Test
    fun shouldSetUserNameAndPassword_whenGivenInConfig() {
        every { mqttConfig.username } returns "expectedUsername"
        every { mqttConfig.password } returns "expectedPassword"

        val slot = slot<MqttConnectionOptions>()
        every { iMqttAsyncClient.connect(capture(slot)) } returns mockk(relaxed = true)

        mqttClient.connect()

        assertThat(slot.captured.userName).isEqualTo("expectedUsername")
        assertThat(slot.captured.password).isEqualTo("expectedPassword".toByteArray())
    }

    @Test
    fun shouldRememberSubscription_whenSubscribe() {
        val expectedMessageListener = mockk<IMqttMessageListener>()

        mqttClient.subscribe("expected/topic/1", 1, expectedMessageListener)
        mqttClient.subscribe("expected/topic/2", 2, expectedMessageListener)
        assertThat(
            mqttClient.subscriptions.map { sub -> Triple(sub.first.topic, sub.first.qos, sub.second) }
                .toSet(),
        ).containsOnly(
            Triple("expected/topic/1", 1, expectedMessageListener),
            Triple("expected/topic/2", 2, expectedMessageListener),
        )
    }

    @Test
    fun shouldSubscribeWithAsyncClient_whenSubscribe() {
        val expectedCallback = mockk<IMqttMessageListener>()

        val slot = slot<MqttSubscription>()

        mqttClient.subscribe("expected/topic/1", 1, expectedCallback)

        verify { iMqttAsyncClient.subscribe(capture(slot), null, null, expectedCallback, any()) }
        assertThat(slot.captured.topic).isEqualTo("expected/topic/1")
        assertThat(slot.captured.qos).isEqualTo(1)
    }

    @Test
    fun shouldSubscribeAgain_whenConnectComplete_andGivenMultipleSubscriptions() {
        val expectedCallback = mockk<IMqttMessageListener>()

        val slot = mutableListOf<MqttSubscription>()

        mqttClient.subscribe("expected/topic/1", 1, expectedCallback)
        mqttClient.subscribe("expected/topic/2", 2, expectedCallback)
        clearAllMocks()

        mqttClient.connectComplete(true, "tcp://example.com")

        verify { iMqttAsyncClient.subscribe(capture(slot), null, null, expectedCallback, any()) }
        assertThat(slot.count()).isEqualTo(2)
    }

    @Test
    fun shouldNotSubscribeAgain_whenConnectComplete_andNotReconnect() {
        val expectedCallback = mockk<IMqttMessageListener>()

        val slot = mutableListOf<MqttSubscription>()

        mqttClient.subscribe("expected/topic/1", 1, expectedCallback)
        mqttClient.subscribe("expected/topic/2", 2, expectedCallback)
        clearAllMocks()

        mqttClient.connectComplete(false, "tcp://example.com")

        verify(exactly = 0) { iMqttAsyncClient.subscribe(capture(slot), null, null, expectedCallback, any()) }
    }

    @Test
    fun shouldNotFail_whenEventsTriggered() {
        mqttClient.disconnected(null)
        mqttClient.mqttErrorOccurred(null)
        mqttClient.messageArrived(null, null)
        mqttClient.deliveryComplete(null)
        mqttClient.authPacketArrived(0, null)
    }

    @Test
    fun shouldRegisterCallback_whenConnect() {
        mqttClient.connect()
        verify { iMqttAsyncClient.setCallback(mqttClient) }
    }
}
