# SONIQ — Feel Every Frequency.

**Phase 2 — Authentication & User System** | Premium Android Music Streaming Platform

## Overview

This repository contains **SONIQ** (`com.soniq.music`) through **Phase 2 — Authentication & User System**, built directly on top of the verified Phase 1 Foundation:
- Single `:app` Gradle module with strict Clean Architecture package boundaries (`app`, `core`, `data`, `domain`, `ui`, `audio`, `database`, `network`, `di`).
- **Supabase Authentication & PostgREST (`supabase-kt` BOM `3.5.0` + Ktor `3.2.1`)**: registration, login, reactive session restoration, sign-out, guest browsing mode, and user profile synchronization.
- **PostgreSQL + Row Level Security (`supabase/migrations/001_phase2_profiles_auth.sql`)**: `public.profiles` schema, auto-provisioning signup trigger, and strict `auth.uid() = id` RLS policies.
- **Dynamic Dark Glassmorphism UI**: Auth screen, live Profile viewer/editor, Dynamic Atmosphere accent switcher, and 5-destination adaptive navigation shell.

## Verified Toolchain & Dependency Matrix

| Component | Version |
| :--- | :--- |
| Application ID | `com.soniq.music` |
| Min SDK / Target & Compile SDK | `26` / `36` (Android 16) |
| JDK Toolchain | `17` |
| Android Gradle Plugin (AGP) / Gradle | `8.13.0` / `8.14.3` |
| Kotlin & Compose Compiler / KSP | `2.2.21` / `2.2.21-2.0.5` |
| Dagger Hilt / Hilt Nav Compose | `2.57.2` / `1.2.0` |
| Jetpack Compose BOM / Navigation Compose | `2025.12.01` / `2.9.7` |
| Supabase Kotlin BOM / Ktor Android | `3.5.0` / `3.2.1` |

## Setup & Supabase Configuration

1. Copy `local.properties.example` to `local.properties` (gitignored) and provide your project's `SUPABASE_URL` and `SUPABASE_ANON_KEY` (never commit secrets or service role keys).
2. Run `supabase/migrations/001_phase2_profiles_auth.sql` in your Supabase project's SQL Editor to create `public.profiles` with Row Level Security enabled.
3. Build and test locally or in Codemagic:
   ```bash
   ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
   ```

## Validation Limitations (Offline Authoring Environment)

Static validation (TOML, XML, Kotlin package-path matching, balanced delimiters, secret/TODO scanning, SQL migration presence, and ZIP integrity) was executed here. Kotlin compilation, JUnit execution, Android Lint, and APK assembly require external verification in Android Studio or Codemagic.
