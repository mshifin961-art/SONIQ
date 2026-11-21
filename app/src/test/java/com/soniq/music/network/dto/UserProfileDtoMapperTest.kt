package com.soniq.music.network.dto

import com.soniq.music.domain.model.ProfileUpdateRequest
import com.soniq.music.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserProfileDtoMapperTest {

    @Test
    fun `toDomain and toDto preserve profile fields and normalize username lowercase`() {
        val dto = UserProfileDto(
            id = "user-123",
            email = "aria@soniq.music",
            username = "Shifin",
            displayName = "Aria Chen",
            avatarUrl = "   ",
            bio = "Ambient & lo-fi collector"
        )

        val domain = dto.toDomain()
        assertEquals("user-123", domain.id)
        assertEquals("shifin", domain.username)
        assertNull(domain.avatarUrl)
        assertEquals("AC", domain.initials)

        val roundTrip = UserProfile(
            id = "user-123",
            email = "aria@soniq.music",
            username = "  ",
            displayName = " Aria Chen ",
            avatarUrl = "https://example.com/p.png",
            bio = " Hi "
        ).toDto()

        assertNull(roundTrip.username)
        assertEquals("Aria Chen", roundTrip.displayName)
        assertEquals("Hi", roundTrip.bio)
    }

    @Test
    fun `ProfileUpdateRequest toUpdateDto normalizes username and only includes permitted fields`() {
        val updateDto = ProfileUpdateRequest(
            userId = "user-123",
            displayName = "  Shifin  ",
            username = "  SHIFIN  ",
            bio = "  Hi-res audio  ",
            avatarUrl = " https://example.com/a.png "
        ).toUpdateDto()

        assertEquals("shifin", updateDto.username)
        assertEquals("Shifin", updateDto.displayName)
        assertEquals("Hi-res audio", updateDto.bio)
        assertEquals("https://example.com/a.png", updateDto.avatarUrl)
    }
}
