package delay.model

import org.eclipse.paho.mqttv5.common.MqttMessage

data class DelayRequest(val period: Long, val topic: String, val reset: Boolean, val mqttMessage: MqttMessage)
