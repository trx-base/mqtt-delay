package delay.mqtt

import assertk.assertThat
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class MqttBeanFactoryTest {

    @InjectMockKs
    @SpyK
    lateinit var mqttBeanFactory: MqttBeanFactory

    @Test
    fun shouldSetExpectedConnectionOptions_whenConnectMqttClient() {
        val mockClient = mockk<MqttAsyncClient>(relaxed = true)
        every { mqttBeanFactory.createClient() } returns mockClient

        val slot = slot<MqttConnectionOptions>()
        every { mockClient.connect(capture(slot)) } returns mockk(relaxed = true)

        mqttBeanFactory.mqttAsyncClient()
        assertThat(slot.captured.isCleanStart).isTrue()
        assertThat(slot.captured.isAutomaticReconnect).isTrue()
    }
}
