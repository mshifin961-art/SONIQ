package com.soniq.music.data.catalogue

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface StarterCollectionLoader {
    fun loadBundle(): StarterCollectionBundle
}

/**
 * Reads `assets/music/licenses.json` from the APK bundle and verifies whether
 * each track's audio binary is present in Android assets.
 */
@Singleton
class StarterCollectionAssetLoader @Inject constructor(
    @ApplicationContext private val context: Context
) : StarterCollectionLoader {

    override fun loadBundle(): StarterCollectionBundle {
        val rawJson = context.assets
            .open(LICENSES_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return StarterLicenseManifestParser.parse(
            rawJson = rawJson,
            assetExistsChecker = { assetPath -> assetExists(assetPath) }
        )
    }

    private fun assetExists(assetPath: String): Boolean = try {
        context.assets.open(assetPath).use { true }
    } catch (_: Exception) {
        false
    }

    private companion object {
        const val LICENSES_ASSET_PATH = "music/licenses.json"
    }
}
