package delay.mqtt

import io.micronaut.context.annotation.Factory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KLogging
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence

@Factory
class MqttAsyncClientFactory {

    private companion object : KLogging()

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Singleton
    fun create(): IMqttAsyncClient {
        val serverURI = mqttConfig.serverURI
        val clientId = mqttConfig.clientId
        val mqttPersistence = MemoryPersistence()

        logger.info { "Creating MqttAsyncClient - serverURI: $serverURI, clientId: $clientId, persistence: $mqttPersistence" }
        return MqttAsyncClient(serverURI, clientId, mqttPersistence)
    }
}
