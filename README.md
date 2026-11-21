# SONIQ — Feel Every Frequency.

**Phase 3 — Music Catalogue (Finalized)** | Premium Android Music Streaming Platform

## Overview

**SONIQ** (`com.soniq.music`) is a production-structured Android music streaming application built phase-by-phase with Clean Architecture, Jetpack Compose + Material 3, Hilt dependency injection, Room local persistence, and Supabase backend integration.

### Completed Phases
- **Phase 1 — Foundation:** Single `:app` Gradle module with strict 9-package Clean Architecture separation (`app`, `core`, `data`, `domain`, `ui`, `audio`, `database`, `network`, `di`), Dynamic Dark Glassmorphism design system (`#090B12` Midnight Black, `#111525` Deep Navy, translucent glass surfaces, live Dynamic Purple / Electric Blue / Neon Pink atmosphere accents), and adaptive 5-destination navigation (`Home`, `Explore`, `Search`, `Library`, `Profile`).
- **Phase 2 — Authentication & User System:** Supabase Auth & PostgREST (`supabase-kt` BOM `3.5.0` + Ktor `3.2.1`), registration, login, explicit persistent session storage (`autoLoadFromStorage`, `autoSaveToStorage`, `alwaysAutoRefresh`), sign-out, Guest mode, Auth-managed email change flow, case-insensitive username uniqueness, and security-hardened PostgreSQL migrations (`001`–`003`) with Row Level Security and table + column-level privilege revocations.
- **Phase 3 — Music Catalogue:**
  - **Local Room Database (`2.8.5`):** `SoniqDatabase`, `CatalogueDao`, `RecentSearchDao`, and entities for `genres`, `artists`, `albums`, `tracks`, and `recent_searches`.
  - **Offline-First Catalogue Repository:** `OfflineFirstCatalogueRepository` combines the bundled Starter Collection, Room local cache, and optional read-only Supabase catalogue synchronization (`004_phase3_music_catalogue.sql`). Works offline without requiring an account.
  - **Multi-Entity Search Infrastructure:** `CatalogueSearchEngine` and `SearchViewModel` provide case-insensitive, relevance-ranked search across Songs, Artists, Albums, and Genres with scope filtering and persistent Recent Searches.
  - **Verified Starter Collection Integration & Licensing Manifest:** `app/src/main/assets/music/licenses.json` defines 10 real, verified CC0 1.0 / Public Domain tracks (2 each across Electronic, Chill / Lo-fi, Instrumental, Ambient, and Pop / Acoustic) with complete source URLs, license URLs, attribution notices, redistribution evidence, and original procedural artwork licensing. `StarterCollectionAssetLoader` inspects `AssetManager` at runtime to report each track's binary availability honestly.

## Verified Toolchain & Dependency Matrix

| Component | Version |
| :--- | :--- |
| Application ID | `com.soniq.music` |
| Min SDK / Target & Compile SDK | `26` / `36` (Android 16) |
| JDK Toolchain | `17` |
| Android Gradle Plugin (AGP) / Gradle | `8.13.0` / `8.14.3` |
| Kotlin & Compose Compiler / KSP | `2.2.21` / `2.2.21-2.0.5` |
| Jetpack Compose BOM | `2025.12.01` |
| Navigation Compose | `2.9.7` |
| Room (Runtime, KTX, KSP Compiler) | `2.8.5` |
| Dagger Hilt / Hilt Nav Compose | `2.57.2` / `1.2.0` |
| Supabase Kotlin BOM / Ktor Android | `3.5.0` / `3.2.1` |
| Codemagic Build Instance | `mac_mini_m2` |

## Supabase Migrations

Apply migrations in order in your Supabase SQL Editor (or via Supabase CLI):
1. `supabase/migrations/001_phase2_profiles_auth.sql` — `public.profiles`, RLS, server-controlled provisioning (`handle_new_user`, `ensure_own_profile`), Auth email sync, case-insensitive username uniqueness, and minimal table/column privileges.
2. `supabase/migrations/002_phase2_profiles_privilege_hardening.sql` — Idempotent privilege hardening for databases upgrading from earlier `001`.
3. `supabase/migrations/003_phase2_profiles_column_privilege_hardening.sql` — Idempotent explicit column-level privilege revocation for databases where `002` was already applied.
4. `supabase/migrations/004_phase3_music_catalogue.sql` — Read-only `public.genres`, `public.artists`, `public.albums`, and `public.tracks` tables with RLS enabled and client write privileges revoked (metadata and HTTPS stream/artwork URLs only; no binary audio stored in PostgreSQL).

## Current Limitations & Pending Items

- **Audio Playback Pending (Phase 4):** Actual audio playback (AndroidX Media3 ExoPlayer, `MediaSessionService`, background playback, queue management, audio focus, and media notifications) is intentionally deferred to **Phase 4 — Streaming Engine**. `NoOpPlaybackController` remains bound in Phase 3.
- **Starter Collection Audio Binaries Pending:** No unverified or unlicensed audio files are bundled. Because this workspace is offline, the 10 verified CC0 / Public Domain `.ogg`/`.mp3` audio files are marked `"assetStatus": "pending_binary_download"` in `app/src/main/assets/music/licenses.json`. The UI honestly displays `"Audio Pending"` until each verified file is downloaded from its documented `sourceUrl` and placed under `app/src/main/assets/music/audio/` matching `bundledAssetPath`.
- **External Build, Test & Database Verification Required:** This workspace has no Android SDK, full JDK (`javac`), Gradle, or PostgreSQL/Supabase instance. Run `./gradlew :app:testDebugUnitTest --stacktrace`, `./gradlew :app:lintDebug --stacktrace`, and `./gradlew :app:assembleDebug --stacktrace` in Android Studio or Codemagic (`mac_mini_m2`) to execute all 19 unit test suites and assemble the APK.

## Local Setup & CI Commands

1. Copy `local.properties.example` to `local.properties` (gitignored) and optionally provide `SUPABASE_URL` and `SUPABASE_ANON_KEY` (never commit secrets or service-role keys).
2. Run Gradle validation locally or via `codemagic.yaml`:
   ```bash
   ./gradlew :app:testDebugUnitTest --stacktrace
   ./gradlew :app:lintDebug --stacktrace
   ./gradlew :app:assembleDebug --stacktrace
   ```
