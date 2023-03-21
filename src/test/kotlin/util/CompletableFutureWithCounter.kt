package util

import mu.KLogging
import java.util.concurrent.CompletableFuture

class CompletableFutureWithCounter<T> : CompletableFuture<T>() {

    private companion object : KLogging()

    var count = 0

    override fun complete(value: T): Boolean {
        count++
        if (count > 1) {
            logger.warn { "Message completed multiple times: $value" }
        }
        return super.complete(value)
    }
}
