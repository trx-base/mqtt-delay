package delay.mqtt

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MqttClientTest {

    @InjectMockKs
    @SpyK
    var mqttClient = MqttClient()

    @RelaxedMockK
    lateinit var iMqttAsyncClient: IMqttAsyncClient

    @Test
    fun shouldUseMqttMessage_whenPublish() {
        val mqttMessage = mockk<MqttMessage>()
        mqttClient.publish("someTopic", mqttMessage)
        verify { iMqttAsyncClient.publish(any(), eq(mqttMessage)) }
    }
}
