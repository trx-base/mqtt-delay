package delay.mqtt

import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KLogging
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties

@Singleton
class MqttClient {

    private companion object : KLogging()

    @Inject
    lateinit var instance: IMqttAsyncClient

    fun publish(topic: String, mqttMessage: MqttMessage) {
        logger.info { "topic: $topic, mqttMessage: $mqttMessage" }
        instance.publish(topic, mqttMessage)
    }

    fun subscribe(topic: String, qos: Int, mqttMessageListener: IMqttMessageListener) {
        logger.info { "topic: $topic, qos: $qos" }
        val mqttSubscription = MqttSubscription(topic, qos)
        instance.subscribe(mqttSubscription, null, null, mqttMessageListener, mqttProperties())
    }

    private fun mqttProperties(): MqttProperties {
        val props = MqttProperties()
        props.subscriptionIdentifiers = arrayListOf(0)
        return props
    }
}
