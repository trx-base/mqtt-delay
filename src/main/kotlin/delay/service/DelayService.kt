package delay.service

import delay.model.DelayRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mqtt.MqttClient
import mu.KLogging

@Singleton
open class DelayService {

    private companion object : KLogging()

    @Inject
    lateinit var mqttClient: MqttClient

    @Inject
    lateinit var storageService: StorageService

    fun delayMessage(delayRequest: DelayRequest) {
        logger.info { "DelayRequest: $delayRequest" }
        if (isNotAlreadyDelayed(delayRequest.topic)) {
            markDelayed(delayRequest.topic)
            CoroutineScope(Dispatchers.IO).launch {
                delay(delayRequest.period * 1000)
                mqttClient.publish(
                    delayRequest.topic,
                    delayRequest.mqttMessage,
                )
                unmarkDelayed(delayRequest.topic)
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
