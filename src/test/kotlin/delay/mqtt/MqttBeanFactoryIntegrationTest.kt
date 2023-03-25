package delay.mqtt

import assertk.assertThat
import assertk.assertions.hasSize
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautTest
class MqttBeanFactoryIntegrationTest {

    @Inject
    lateinit var mqttBeanFactory: MqttBeanFactory

    @Test
    fun shouldUseDifferentClientId_whenMqttAsyncClient_givenDifferentClientIdInConfig() {
        val clientIdSet = mutableSetOf<String>()

        for (i in 1..5) {
            clientIdSet.add(mqttBeanFactory.mqttAsyncClient().clientId)
        }
        assertThat(clientIdSet).hasSize(5)
    }
}
