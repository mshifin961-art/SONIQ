package com.soniq.music.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Root Application entry point for SONIQ.
 * Initializes the Hilt dependency injection graph at process startup.
 */
@HiltAndroidApp
class SoniqApplication : Application()
