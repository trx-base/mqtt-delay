package delay.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
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
        storageService.engine.put("key-42", "Expected value")
        assertThat(storageService.get("key-42")).isEqualTo("Expected value")
    }

    @Test
    fun shouldPersistValueInEngine_whenPut() {
        storageService.put("key-42", "Expected value")
        assertThat(storageService.engine.get("key-42")).isEqualTo("Expected value")
    }

    @Test
    fun shouldRemoveEntryFromStorage_whenRemove() {
        storageService.put("key-42", "A value")
        storageService.remove("key-42")
        assertThat(storageService.get("key-42")).isNull()
    }
}
