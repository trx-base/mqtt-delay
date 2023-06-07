package delay.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class StorageServiceTest {

    @InjectMockKs
    var storageService: StorageService = StorageService()

    @Test
    fun shouldBeInitialized() {
        assertThat(storageService).isNotNull()
    }

    @Test
    fun shouldReturnNull_whenGet_givenNonExistentKey() {
        assertThat(storageService.get("key")).isNull()
    }

    @Test
    fun shouldReturnValue_whenGet_givenExistingEntryInEngine() {
        val expected = mockk<CoroutineScope>()
        storageService.engine.put("key-42", expected)
        assertThat(storageService.get("key-42")).isEqualTo(expected)
    }

    @Test
    fun shouldPersistValueInEngine_whenPut() {
        val expected = mockk<CoroutineScope>()
        storageService.put("key-42", expected)
        assertThat(storageService.engine.get("key-42")).isEqualTo(expected)
    }

    @Test
    fun shouldRemoveEntryFromStorage_whenRemove() {
        val expected = mockk<CoroutineScope>()
        storageService.put("key-42", expected)
        storageService.remove("key-42")
        assertThat(storageService.get("key-42")).isNull()
    }
}
