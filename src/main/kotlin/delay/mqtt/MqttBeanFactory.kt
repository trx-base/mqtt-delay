package delay.mqtt

import io.micronaut.context.annotation.Factory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KLogging
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptionsBuilder
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence

@Factory
class MqttBeanFactory {

    private companion object : KLogging()

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Singleton
    fun mqttAsyncClient(): IMqttAsyncClient {
        logger.info { "Creating connection to MQTT Broker, serverURI: ${mqttConfig.serverURI}, topic: $${mqttConfig.topic}" }
        val client = createClient()
        logger.info { "Connecting clientId: ${client.clientId}" }
        val mqttConnectionOptions = MqttConnectionOptionsBuilder().cleanStart(true).automaticReconnect(true).build()
        client.connect(mqttConnectionOptions).waitForCompletion()
        logger.info { "MQTT Broker connected." }
        return client
    }

    fun createClient(): MqttAsyncClient {
        val mqttDefaultFilePersistence = MqttDefaultFilePersistence("mqtt-persistence")
        val clientId = generateUniqueClientId()
        return MqttAsyncClient(mqttConfig.serverURI, clientId, mqttDefaultFilePersistence)
    }

    private fun generateUniqueClientId(): String {
        return mqttConfig.topic + "_" + mqttConfig.clientId
    }
}
