package delay.mqtt

import org.eclipse.paho.mqttv5.client.IMqttAsyncClient

interface Subscription {

    fun registerSubscriptions(mqttClient: IMqttAsyncClient)
}
