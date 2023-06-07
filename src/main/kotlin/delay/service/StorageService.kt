package delay.service

import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import mu.KLogging

@Singleton
class StorageService {

    private companion object : KLogging()

    val engine = HashMap<String, Any>()

    fun get(key: String): Any? {
        logger.debug("get() - key: $key - value: ${engine[key]}")
        if (engine.containsKey(key)) {
            return engine[key]!!
        }
        return null
    }

    fun put(key: String, value: CoroutineScope) {
        logger.debug("put() - key: $key - value: $value")
        engine[key] = value
    }

    fun remove(key: String) {
        logger.debug("remove() - key: $key")
        engine.remove(key)
    }
}
