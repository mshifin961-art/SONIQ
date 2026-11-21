package com.soniq.music.network

import com.soniq.music.core.common.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AuthErrorMapperTest {

    @Test
    fun `map translates IOException to retryable Network error`() {
        val mapped = AuthErrorMapper.map(IOException("Connection reset"))
        assertTrue(mapped is AppError.Network)
        assertTrue((mapped as AppError.Network).isRetryable)
    }

    @Test
    fun `mapRestMessage handles invalid credentials and unconfirmed email cleanly`() {
        val invalidCreds = AuthErrorMapper.mapRestMessage("Invalid login credentials", 400)
        assertTrue(invalidCreds is AppError.Authentication)
        assertTrue(invalidCreds.message.contains("Incorrect email or password"))

        val unconfirmed = AuthErrorMapper.mapRestMessage("Email not confirmed", 400)
        assertTrue(unconfirmed is AppError.Authentication)
        assertTrue(unconfirmed.message.contains("confirm your email"))
    }

    @Test
    fun `mapRestMessage maps case-insensitive duplicate username index to username Validation error`() {
        val dupIndex = AuthErrorMapper.mapRestMessage(
            "duplicate key value violates unique constraint idx_profiles_username_lower_unique",
            409
        )
        assertTrue(dupIndex is AppError.Validation)
        assertEquals("username", (dupIndex as AppError.Validation).field)

        val dupCode = AuthErrorMapper.mapRestMessage("ERROR 23505 on username", 409)
        assertTrue(dupCode is AppError.Validation)
        assertEquals("username", (dupCode as AppError.Validation).field)
    }

    @Test
    fun `mapRestMessage maps direct email overwrite trigger rejection to Validation error`() {
        val emailProtected = AuthErrorMapper.mapRestMessage(
            "Profile email is managed by Supabase Auth and cannot be modified via profile updates.",
            403
        )
        assertTrue(emailProtected is AppError.Validation)
        assertEquals("email", (emailProtected as AppError.Validation).field)
    }

    @Test
    fun `mapRestMessage maps 5xx errors to Network error`() {
        val serverErr = AuthErrorMapper.mapRestMessage("Internal error", 503)
        assertTrue(serverErr is AppError.Network)
    }
}
