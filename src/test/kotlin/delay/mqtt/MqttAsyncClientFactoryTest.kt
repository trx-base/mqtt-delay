package delay.mqtt

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class MqttAsyncClientFactoryTest {

    @InjectMockKs
    lateinit var mqttAsyncClientFactory: MqttAsyncClientFactory

    @MockK(relaxed = true)
    lateinit var mqttConfig: MqttConfig

    @Test
    fun shouldCreateClientWithExpectedParams_whenCreate() {
        every { mqttConfig.serverURI } returns "ssl://expected.uri"
        every { mqttConfig.clientId } returns "expectedClientId"

        val client = mqttAsyncClientFactory.create()
        assertThat(client.serverURI).isEqualTo("ssl://expected.uri")
        assertThat(client.clientId).isEqualTo("expectedClientId")
    }
}
