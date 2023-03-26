package delay.mqtt

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MqttClientTest {

    @InjectMockKs
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
}
