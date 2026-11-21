# SONIQ — Feel Every Frequency.

**Phase 1 — Foundation** | Premium Android Music Streaming Platform

## Overview

This archive contains the Phase 1 Foundation of **SONIQ** (`com.soniq.music`): a single-module (`:app`) Android application built with Clean Architecture, strict package boundaries, Hilt dependency injection, Jetpack Compose + Material 3, and SONIQ's original **Dynamic Dark Glassmorphism** visual system.

## Verified Toolchain & Dependency Matrix

| Component | Version |
| :--- | :--- |
| Application ID | `com.soniq.music` |
| Min SDK | `26` (Android 8.0) |
| Compile / Target SDK | `36` (Android 16) |
| JDK Toolchain | `17` |
| Android Gradle Plugin (AGP) | `8.13.0` |
| Gradle Wrapper | `8.14.3` |
| Kotlin & Compose Compiler | `2.2.21` |
| KSP | `2.2.21-2.0.5` |
| Dagger Hilt | `2.57.2` |
| Jetpack Compose BOM | `2026.09.00` |
| Navigation Compose | `2.9.7` |
| Coroutines | `1.10.1` |

## Package Architecture (`com.soniq.music`)

- `app/` — `SoniqApplication` (`@HiltAndroidApp`), `MainActivity` (edge-to-edge host)
- `core/` — `AppResult`, `AppError`, `UiState`, `SoniqDispatchers`, `TimeFormatter`, `AppConfig`, `SupabaseConfig`
- `domain/` — Pure Kotlin models (`AtmosphereAccent`, `AudioQualityTier`, `PlaybackStatus`, `RepeatMode`) and repository contracts
- `data/` — Reactive repository implementations (`InMemoryAtmospherePreferencesRepository`)
- `audio/` — UI-independent playback state and controller contracts (`PlaybackSnapshot`, `PlaybackController`, `NoOpPlaybackController`) decoupled from Compose and Activity lifecycle
- `database/` — Local Room database naming & table contracts (`SoniqDatabaseContract`)
- `network/` — Build-injected Supabase environment readiness validator (`SupabaseEnvironmentValidator`)
- `di/` — Hilt singleton modules (`DispatchersModule`, `ConfigModule`, `AppBindingsModule`)
- `ui/` — Dynamic Dark Glassmorphism theme, reusable glass components, type-safe 5-destination navigation shell (`Home`, `Explore`, `Search`, `Library`, `Profile`), and adaptive bottom bar / navigation rail layout

## Getting Started (Android Studio or Codemagic)

1. Copy `local.properties.example` to `local.properties` (gitignored).
2. Open the `soniq/` folder in Android Studio (Narwhal 2025.1+ / 2026) with JDK 17 and Android SDK Platform 36 installed, or connect the repo to Codemagic using `codemagic.yaml`.
3. Run verification locally:
   ```bash
   ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
   ```
   *(If `gradlew` has not been bootstrapped yet, run `gradle wrapper --gradle-version 8.14.3` once or let Android Studio sync generate it.)*

## Validation Limitations (Offline Authoring Environment)

This phase was authored in an offline Linux environment without the Android SDK, full JDK (`javac`), Gradle, or internet access. Static validation (TOML, XML, Kotlin package/path consistency, balanced syntax tokens, secret scanning, and ZIP integrity) was executed, while Kotlin compilation, JUnit execution, Android Lint, and APK assembly must be run in Android Studio or Codemagic.
