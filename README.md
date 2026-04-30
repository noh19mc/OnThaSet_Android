# OnThaSet_Android

**Android port by NOH (Maurice Grose)** ([@noh19mc](https://github.com/noh19mc)) · iOS original by [@RHayes1302](https://github.com/RHayes1302)

Android port of the **On Tha Set** iOS app — a community platform for motorcycle riders to discover and share local rides, rallies, MC annuals, charity events, and more. The iOS version is on the App Store; this repo is the in-progress Android sibling that talks to the same Supabase backend so users on both platforms share data.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3, edge-to-edge)
- **Hilt** for DI
- **Navigation Compose** for routing
- **Supabase Kotlin SDK** (`auth-kt`, `postgrest-kt`, `storage-kt`, `realtime-kt`) — shares the iOS app's project
- **Coil** for async image loading
- **Maps Compose** + Google Maps SDK for the National Calendar map view
- **Ktor** HTTP client (with JSON content negotiation) for non-Supabase APIs (Open-Meteo)
- **kotlinx-datetime** for event timestamps
- **AndroidX DataStore** for local prefs
- Built with **Gradle 9.5** + **AGP 8.13.2**, JDK 17 toolchain, `minSdk 26` / `targetSdk 35`

## Project layout

```
app/src/main/java/com/onthaset/app/
├── auth/         # Supabase email/password sign-in, sign-up, email confirmation, password reset
├── events/       # Events list, detail, and the National Run Calendar
├── profile/      # User profile view + edit (text fields and photos)
├── bikes/        # Bike Builds feed + create with before/after photos
├── eventphotos/  # Ride Photos feed + upload (event_photos table)
├── ads/          # AdMob banner Composable
├── admin/        # PIN-gated event moderation
├── weather/      # 5-day Ride Forecast (Open-Meteo) with rider safety chip
├── imaging/      # Image compression + Supabase Storage upload helpers
├── home/         # Logged-in landing screen
└── navigation/   # Routes + auth-gated NavHost
```

## Getting started

1. **Prereqs** — install [Android Studio](https://developer.android.com/studio) and let it run the first-launch wizard so the SDK lands at `~/Library/Android/sdk`.
2. **Clone:**
   ```sh
   git clone git@github.com:noh19mc/OnThaSet_Android.git
   cd OnThaSet_Android
   ```
3. **Backend keys** — create `local.properties` (gitignored) with the Supabase project credentials:
   ```properties
   sdk.dir=/Users/<you>/Library/Android/sdk
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<anon-jwt>
   # Optional — needed only for the National Calendar map view.
   # Get one at console.cloud.google.com/google/maps-apis and restrict it to "Maps SDK for Android".
   MAPS_API_KEY=
   ```
4. **Build:**
   ```sh
   ./gradlew :app:assembleDebug
   ```
5. **Run** — open the project in Android Studio, start an AVD, hit ▶.

## Backend

Both clients use the same Supabase project. Tables touched by Android so far:

| Table  | Columns Android reads/writes                                                                       |
| ------ | -------------------------------------------------------------------------------------------------- |
| `users`  | `apple_user_id`, `email`, `display_name`, `bio`, `hometown`, `club`, `favorite_ride`, `riding_since`, `preferred_ride_type`, `favorite_route`, `instagram_handle`, `tiktok_handle`, `youtube_channel`, `facebook_handle`, `profile_image_url`, `background_image_url` |
| `events` | `id`, `title`, `date`, `category`, `location_name`, `details`, `price`, `latitude`, `longitude`, `posted_by_user_id`, `posted_by_name`, `created_at`, `image_url` |
| `bike_builds` | `id`, `user_id`, `modification_title`, `note`, `before_image_url`, `after_image_url`, `bike_make`, `bike_model`, `bike_year`, `created_at` |
| `event_photos` | `id`, `uploaded_by`, `event_name`, `event_date`, `location`, `caption`, `image_url`, `created_at` |

Storage buckets: `profile-images` (avatar + cover), `event-flyers` (event images), `bike-progress` (before/after build photos), `event-photos` (ride photos).

> Note: the iOS app stores the Supabase auth UUID in the `apple_user_id` column even for email-signup users. Android matches that convention so both clients read and write the same row per user.

## Status

- [x] Project scaffold + signed-commit pipeline
- [x] Auth — email/password, sign-up, password reset, session persistence, "Explore Without Signing In" guest mode
- [x] Events — upcoming list with iOS-matching weekend cutoff, detail screen
- [x] Profile — view + edit text fields, profile/cover photo upload
- [x] National Run Calendar — list view with month picker + category filter chips
- [x] Ride Forecast — Open-Meteo 5-day forecast with rider safety messaging based on wind speed
- [x] Bike Builds — feed + post with before/after photos
- [x] Event creation — title, category, date+time picker, pipe-delimited location, optional flyer upload, address geocoded via Nominatim so events land on the National Map
- [x] National Calendar map view — Compose Google Maps with category-colored pins (requires `MAPS_API_KEY`; renders a "no key" placeholder otherwise)
- [x] Ride Photos — feed + upload (event_photos table, event-photos bucket)
- [x] AdMob banner — anchored on Home (renders only when `ADMOB_APP_ID` + `ADMOB_BANNER_UNIT_ID` are configured; SDK falls back to Google's test app ID otherwise so the SDK still initializes cleanly)
- [x] Admin v1 — PIN-gated event moderation (delete events). Reports / ad approvals deferred.
- [x] Onboarding wizard — single-screen welcome that captures display name, hometown, bio, bike, riding-since, and club for fresh accounts; surfaces as a "Finish Setting Up Your Profile" CTA on Home until those fields are filled.
- [ ] Google Play Billing — replaces the iOS StoreKit subscription / per-event purchase flow
- [ ] Ride Forecast — 5-day weather for current location or event location
- [ ] National Calendar map — Compose Google Maps with state-shaped overlays
- [ ] AdMob banner placements
- [ ] Admin panel

## Differences from iOS (intentional, for now)

- **No Sign in with Apple** — Android equivalent would be Google Sign In; deferred until needed.
- **Local persistence** — iOS uses SwiftData; Android currently re-queries Supabase per screen. Will add Room if/when offline support is needed.
- **National Run map** — Android has the list view but not the WebKit US map. Ported separately when we tackle maps.
- **Photo upload max dim** — Android downscales to 1600px long edge / JPEG 85; the iOS compressor's targets may differ slightly.
