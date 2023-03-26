package delay.mqtt

import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KLogging
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.client.MqttConnectionOptionsBuilder
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import javax.annotation.PostConstruct

@Singleton
class MqttClient {

    private companion object : KLogging()

    @Inject
    lateinit var mqttAsyncClient: IMqttAsyncClient

    @Inject
    lateinit var mqttConfig: MqttConfig

    @PostConstruct
    fun postConstruct() {
        connect()
    }

    fun connect() {
        logger.info { "Connecting clientId: ${mqttAsyncClient.clientId}" }
        val mqttConnectionOptions =
            MqttConnectionOptionsBuilder().cleanStart(true).automaticReconnect(true).username(mqttConfig.username)
                .password(mqttConfig.password?.toByteArray())
                .build()
        mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion()
        logger.info { "MQTT Broker connected." }
    }

    fun publish(topic: String, mqttMessage: MqttMessage) {
        logger.info { "topic: $topic, mqttMessage: $mqttMessage" }
        mqttAsyncClient.publish(topic, mqttMessage)
    }

    fun subscribe(topic: String, qos: Int, mqttMessageListener: IMqttMessageListener) {
        logger.info { "topic: $topic, qos: $qos" }
        val mqttSubscription = MqttSubscription(topic, qos)
        mqttAsyncClient.subscribe(mqttSubscription, null, null, mqttMessageListener, mqttProperties())
    }

    private fun mqttProperties(): MqttProperties {
        val props = MqttProperties()
        props.subscriptionIdentifiers = arrayListOf(0)
        return props
    }
}
