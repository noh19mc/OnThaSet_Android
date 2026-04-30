# OnThaSet_Android

**Android port by NOH (Maurice Grose)** ([@noh19mc](https://github.com/noh19mc)) · iOS original by [@RHayes1302](https://github.com/RHayes1302)

Android port of the **On Tha Set** iOS app — a community platform for motorcycle riders to discover and share local rides, rallies, MC annuals, charity events, and more. The iOS version is on the App Store; this repo is the in-progress Android sibling that talks to the same Supabase backend so users on both platforms share data.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3, edge-to-edge)
- **Hilt** for DI
- **Navigation Compose** for routing
- **Supabase Kotlin SDK** (`auth-kt`, `postgrest-kt`, `storage-kt`, `realtime-kt`) — shares the iOS app's project
- **Coil** for async image loading
- **kotlinx-datetime** for event timestamps
- **AndroidX DataStore** for local prefs
- Built with **Gradle 9.5** + **AGP 8.13.2**, JDK 17 toolchain, `minSdk 26` / `targetSdk 35`

## Project layout

```
app/src/main/java/com/onthaset/app/
├── auth/         # Supabase email/password sign-in, sign-up, password reset
├── events/       # Events list, detail, and the National Run Calendar
├── profile/      # User profile view + edit (text fields and photos)
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

Storage buckets: `profile-images` (avatar + cover), `event-flyers` (event images).

> Note: the iOS app stores the Supabase auth UUID in the `apple_user_id` column even for email-signup users. Android matches that convention so both clients read and write the same row per user.

## Status

- [x] Project scaffold + signed-commit pipeline
- [x] Auth — email/password, sign-up, password reset, session persistence, "Explore Without Signing In" guest mode
- [x] Events — upcoming list with iOS-matching weekend cutoff, detail screen
- [x] Profile — view + edit text fields, profile/cover photo upload
- [x] National Run Calendar — list view with month picker + category filter chips
- [ ] Bike Builds — list + create with before/after photos
- [ ] Event creation + Google Play Billing (replaces the iOS StoreKit flow)
- [ ] Ride Forecast — 5-day weather for current location or event location
- [ ] National Calendar map — Compose Google Maps with state-shaped overlays
- [ ] AdMob banner placements
- [ ] Admin panel

## Differences from iOS (intentional, for now)

- **No Sign in with Apple** — Android equivalent would be Google Sign In; deferred until needed.
- **Local persistence** — iOS uses SwiftData; Android currently re-queries Supabase per screen. Will add Room if/when offline support is needed.
- **National Run map** — Android has the list view but not the WebKit US map. Ported separately when we tackle maps.
- **Photo upload max dim** — Android downscales to 1600px long edge / JPEG 85; the iOS compressor's targets may differ slightly.
