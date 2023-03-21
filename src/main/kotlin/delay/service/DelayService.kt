package delay.service

import delay.mqtt.MqttClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import org.eclipse.paho.mqttv5.common.MqttMessage

@Singleton
open class DelayService {

    private companion object : KLogging()

    @Inject
    lateinit var mqttClient: MqttClient

    @Inject
    lateinit var storageService: StorageService

    fun delayMessage(period: Long, topic: String, mqttMessage: MqttMessage) {
        logger.info { "period: $period, topic: $topic, mqttMessage: ${mqttMessage.toDebugString()}" }
        if (isNotAlreadyDelayed(topic)) {
            markDelayed(topic)
            CoroutineScope(Dispatchers.IO).launch {
                delay(period * 1000)
                mqttClient.publish(
                    topic,
                    mqttMessage,
                )
                unmarkDelayed(topic)
            }
        } else {
            logger.warn { "Ignoring delayed message. Already delayed!" }
        }
    }

    private fun isNotAlreadyDelayed(topic: String) = storageService.get(topic) != "true"

    private fun markDelayed(topic: String) {
        storageService.put(topic, "true")
    }

    private fun unmarkDelayed(topic: String) {
        storageService.remove(topic)
    }
}
