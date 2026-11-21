package com.soniq.music.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInputValidatorTest {

    @Test
    fun `validateEmail normalizes valid email and rejects malformed input`() {
        val valid = AuthInputValidator.validateEmail("  Listener@Soniq.Music  ")
        assertTrue(valid.isSuccess)
        assertEquals("listener@soniq.music", valid.getOrNull())

        assertTrue(AuthInputValidator.validateEmail("").isError)
        assertTrue(AuthInputValidator.validateEmail("not-an-email").isError)
    }

    @Test
    fun `validateLoginPassword rejects blank and accepts non-blank`() {
        assertTrue(AuthInputValidator.validateLoginPassword("").isError)
        assertTrue(AuthInputValidator.validateLoginPassword("   ").isError)
        assertTrue(AuthInputValidator.validateLoginPassword("anypass").isSuccess)
    }

    @Test
    fun `validateRegistrationPassword enforces length letters and digits`() {
        assertTrue(AuthInputValidator.validateRegistrationPassword("short1").isError)
        assertTrue(AuthInputValidator.validateRegistrationPassword("onlyletters").isError)
        assertTrue(AuthInputValidator.validateRegistrationPassword("12345678").isError)
        assertTrue(AuthInputValidator.validateRegistrationPassword("Soniq2026").isSuccess)
    }

    @Test
    fun `validateDisplayName enforces min and max length`() {
        assertTrue(AuthInputValidator.validateDisplayName("A").isError)
        assertTrue(AuthInputValidator.validateDisplayName("A".repeat(41)).isError)
        val ok = AuthInputValidator.validateDisplayName("  Aria Chen  ")
        assertEquals("Aria Chen", ok.getOrNull())
    }

    @Test
    fun `validateUsernameOptional normalizes case-insensitively and enforces pattern`() {
        assertEquals("", AuthInputValidator.validateUsernameOptional("   ").getOrNull())
        assertEquals("shifin", AuthInputValidator.validateUsernameOptional("Shifin").getOrNull())
        assertEquals("shifin", AuthInputValidator.validateUsernameOptional("shifin").getOrNull())
        assertEquals("shifin", AuthInputValidator.validateUsernameOptional("SHIFIN").getOrNull())
        assertEquals("aria.chen_1", AuthInputValidator.validateUsernameOptional("Aria.Chen_1").getOrNull())
        assertTrue(AuthInputValidator.validateUsernameOptional("ab").isError)
        assertTrue(AuthInputValidator.validateUsernameOptional("bad name!").isError)
    }

    @Test
    fun `validateAvatarUrlOptional allows blank and requires https scheme`() {
        assertNull(AuthInputValidator.validateAvatarUrlOptional("  ").getOrNull())
        assertTrue(AuthInputValidator.validateAvatarUrlOptional("http://example.com/a.png").isError)
        assertEquals(
            "https://cdn.example.com/avatar.png",
            AuthInputValidator.validateAvatarUrlOptional("https://cdn.example.com/avatar.png").getOrNull()
        )
    }
}
