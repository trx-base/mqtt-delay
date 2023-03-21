package delay.mqtt

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("mqtt")
class MqttConfig {
    lateinit var serverURI: String
    lateinit var clientId: String
    lateinit var topic: String
}
