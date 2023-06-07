package delay.service

import delay.model.DelayRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
        logger.info("Delay message: $delayRequest")
        if (isNotAlreadyDelayed(delayRequest)) {
            startDelay(delayRequest)
        } else if (delayRequest.reset) {
            cancelDelay(delayRequest)
            startDelay(delayRequest)
        } else {
            logger.warn { "Ignoring delayed message. Already delayed!" }
        }
    }

    private fun startDelay(delayRequest: DelayRequest) {
        logger.info("Start delay.")
        val routine = CoroutineScope(Dispatchers.IO)
        markDelayed(delayRequest.topic, routine)
        routine.launch {
            delay(delayRequest.period * 1000)
            mqttClient.publish(
                delayRequest.topic,
                delayRequest.mqttMessage,
            )
            unmarkDelayed(delayRequest.topic)
        }
    }

    private fun cancelDelay(delayRequest: DelayRequest) {
        logger.info("Cancel delay.")
        val routine = storageService.get(delayRequest.topic) as CoroutineScope
        routine.cancel()
    }

    private fun markDelayed(topic: String, coroutineScope: CoroutineScope) {
        logger.info("Mark delayed: $topic")
        storageService.put(topic, coroutineScope)
    }

    private fun unmarkDelayed(topic: String) {
        logger.info("Unmark delayed: $topic")
        storageService.remove(topic)
    }

    private fun isNotAlreadyDelayed(delayRequest: DelayRequest): Boolean {
        val entry = storageService.get(delayRequest.topic)
        logger.info("Entry: $entry")
        return storageService.get(delayRequest.topic) == null
    }
}
