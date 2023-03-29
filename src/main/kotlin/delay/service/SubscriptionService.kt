package delay.service

import io.micronaut.context.annotation.Context
import jakarta.inject.Inject
import mqtt.MqttClient
import mqtt.MqttConfig
import mu.KLogging
import org.eclipse.paho.mqttv5.common.MqttMessage
import javax.annotation.PostConstruct

@Context
class SubscriptionService {

    private companion object : KLogging()

    @Inject
    lateinit var mqttConfig: MqttConfig

    @Inject
    lateinit var mqttClient: MqttClient

    @Inject
    lateinit var delayService: DelayService

    @PostConstruct
    fun registerSubscriptions() {
        logger.info { "Registering subscriptions." }
        mqttClient.subscribe("${mqttConfig.topic}/#", 2, this::handleDelayedMessage)
        mqttClient.subscribe("+/${mqttConfig.topic}/#", 2, this::handleDelayedMessage)
    }

    fun handleDelayedMessage(topic: String, mqttMessage: MqttMessage) {
        logger.info { "Handling delayed message. topic: $topic, mqttMessage: ${mqttMessage.toDebugString()}" }
        try {
            val (period, delayedTopic) = parseDelayedPeriodAndTopic(topic)
            delayService.delayMessage(period, delayedTopic, mqttMessage)
        } catch (t: Throwable) {
            logger.error { t }
        }
    }

    fun parseDelayedPeriodAndTopic(topic: String): Pair<Long, String> {
        try {
            val regex = """${mqttConfig.topic}/(\d+)/(.+)""".toRegex()
            val matchResult = regex.find(topic)

            val (period, delayedTopic) = matchResult!!.destructured
            return Pair(period.toLong(), delayedTopic)
        } catch (ex: NullPointerException) {
            throw RuntimeException("Parsing of delayed period and topic failed. Provided string: $topic", ex)
        }
    }
}
