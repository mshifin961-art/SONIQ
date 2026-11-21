package com.soniq.music.data.catalogue

import com.soniq.music.domain.model.TrackAudioSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StarterLicenseManifestParserTest {

    private fun readBundledManifestJson(): String {
        val candidates = listOf(
            File("src/main/assets/music/licenses.json"),
            File("app/src/main/assets/music/licenses.json")
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate assets/music/licenses.json from working dir ${File(".").absolutePath}")
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun `starter manifest contains exactly 10 licensed tracks with 2 per required category`() {
        val rawJson = readBundledManifestJson()
        val bundle = StarterLicenseManifestParser.parse(rawJson) { false }

        assertEquals(10, bundle.tracks.size)
        assertEquals(5, bundle.genres.size)

        val countsByCategory = bundle.tracks.groupingBy { it.categoryName }.eachCount()
        assertEquals(2, countsByCategory["Electronic"])
        assertEquals(2, countsByCategory["Chill / Lo-fi"])
        assertEquals(2, countsByCategory["Instrumental"])
        assertEquals(2, countsByCategory["Ambient"])
        assertEquals(2, countsByCategory["Pop / Acoustic"])
    }

    @Test
    fun `every starter track has complete CC0 or Public Domain licensing metadata`() {
        val rawJson = readBundledManifestJson()
        val bundle = StarterLicenseManifestParser.parse(rawJson) { path ->
            path.endsWith("starter-electronic-01.ogg")
        }

        bundle.tracks.forEach { track ->
            assertTrue(track.isStarterTrack)
            val license = track.licenseInfo
            assertNotNull("Missing licenseInfo for ${track.id}", license)
            requireNotNull(license)
            assertTrue(license.sourceUrl.startsWith("https://"))
            assertTrue(license.licenseUrl.startsWith("https://"))
            assertTrue(
                license.licenseName.contains("CC0", ignoreCase = true) ||
                    license.licenseName.contains("Public Domain", ignoreCase = true)
            )
            assertTrue(license.redistributionEvidence.isNotBlank())
            assertTrue(license.artworkLicense.isNotBlank())
        }

        val first = bundle.tracks.first { it.id == "starter-electronic-01" }
        assertTrue((first.audioSource as TrackAudioSource.BundledAsset).isBinaryBundled)

        val second = bundle.tracks.first { it.id == "starter-electronic-02" }
        assertFalse((second.audioSource as TrackAudioSource.BundledAsset).isBinaryBundled)
    }
}
