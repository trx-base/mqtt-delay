package delay.mqtt

import jakarta.inject.Inject
import jakarta.inject.Singleton
import mu.KLogging
import org.eclipse.paho.mqttv5.client.IMqttAsyncClient
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttConnectionOptionsBuilder
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.MqttSubscription
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import javax.annotation.PostConstruct

@Singleton
class MqttClient : MqttCallback {

    private companion object : KLogging()

    @Inject
    lateinit var mqttAsyncClient: IMqttAsyncClient

    @Inject
    lateinit var mqttConfig: MqttConfig

    val subscriptions = mutableSetOf<Pair<MqttSubscription, IMqttMessageListener>>()

    @PostConstruct
    fun postConstruct() {
        connect()
    }

    fun connect() {
        logger.info { "Connecting. clientId: ${mqttAsyncClient.clientId}" }
        val mqttConnectionOptions =
            MqttConnectionOptionsBuilder().cleanStart(true).automaticReconnect(true).username(mqttConfig.username)
                .password(mqttConfig.password?.toByteArray())
                .build()
        mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion()
        mqttAsyncClient.setCallback(this)
        logger.info { "MQTT Broker connected." }
    }

    fun publish(topic: String, mqttMessage: MqttMessage) {
        logger.info { "Publishing. topic: $topic, mqttMessage: $mqttMessage" }
        mqttAsyncClient.publish(topic, mqttMessage)
    }

    fun subscribe(topic: String, qos: Int, mqttMessageListener: IMqttMessageListener) {
        logger.info { "Subscribing. topic: $topic, qos: $qos" }
        val mqttSubscription = MqttSubscription(topic, qos)
        mqttAsyncClient.subscribe(mqttSubscription, null, null, mqttMessageListener, mqttProperties())
        subscriptions.add(Pair(mqttSubscription, mqttMessageListener))
    }

    override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
        logger.warn { "Disconnected: $disconnectResponse" }
    }

    override fun mqttErrorOccurred(exception: MqttException?) {
        logger.error("MQTT error occured.", exception)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        logger.debug("Message arrived: $message")
    }

    override fun deliveryComplete(token: IMqttToken?) {
        logger.debug("Delivery complete.")
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        logger.warn("Connection complete. reconnect: $reconnect, serverURI: $serverURI")
        if (reconnect) {
            logger.info { "Re-subscribe subscriptions: $subscriptions" }
            for (sub in subscriptions) {
                mqttAsyncClient.subscribe(sub.first, null, null, sub.second, mqttProperties())
            }
        }
    }

    override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
        logger.debug("Auth packet arrived. reasonCode: $reasonCode, properties: $properties")
    }

    private fun mqttProperties(): MqttProperties {
        val props = MqttProperties()
        props.subscriptionIdentifiers = arrayListOf(0)
        return props
    }
}
