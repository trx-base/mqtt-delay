package integration.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

private class CompletableFutureWithCounterTest {

    @Test
    fun shouldProvideInvocationCount_whenCompletedMultipleTimes() {
        val completableFutureWithCounter = CompletableFutureWithCounter<String>()
        for (i in 1..5) {
            completableFutureWithCounter.complete("666")
        }
        assertThat(completableFutureWithCounter.count).isEqualTo(5)
    }
}
