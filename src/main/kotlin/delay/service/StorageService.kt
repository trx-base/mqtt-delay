package delay.service

import jakarta.inject.Singleton
import mu.KLogging

@Singleton
class StorageService {

    private companion object : KLogging()

    val engine = HashMap<String, String>()

    fun get(key: String): String? {
        logger.debug("get() - key: $key - value: ${engine[key]}")
        return engine[key]
    }

    fun put(key: String, value: String) {
        logger.debug("put() - key: $key - value: $value")
        engine[key] = value
    }

    fun remove(key: String) {
        logger.debug("remove() - key: $key")
        engine.remove(key)
    }
}
