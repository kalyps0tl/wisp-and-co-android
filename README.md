# Wisp & Co. 🌿

**Native Android living wallpaper and companion world**, intended for eventual distribution on Google Play. Wisp is an autonomous spirit living in a gentle, explorable storybook setting whose habits, memories and appearance can evolve. The phone version works independently; compatible Wear OS watches may eventually extend the same world.

**Wisp & Co. is Android-wide, NOT exclusive to any Pixel model or Pixel Watch.**

## Current development prototype

The v0.3 explorer/wallpaper foundation includes:

- Native Android `WallpaperService` + an interactive companion app.
- Five separate scenes: dock, garden, pond, forest and furnished cottage interior.
- Temporary Canvas art, animated Wisp, pond ripples, bees and dragonflies.
- Tap-to-move character movement, simple discovery choices and a persistent journal.
- Four distinct spirit silhouettes (woodland, antlered, moth, sprout) and magical markings; reversible appearance evolution based on in-world experiences.
- Local project progression and some opt-in seasonal interactions: pumpkin carving, tree decorating, Hanukkah-themed candle display (an optional depiction, not religious observance).
- Manual weather previews and opt-in approximate-location weather fetched from Open-Meteo.
- **v0.4 context dashboard** showing local time-of-day, battery/charging and battery saver, manual home/away, manual walking/resting, and opt-in Android step-counter hints while the app or live wallpaper is visible.
- Context gently nudges autonomous activity choices; a user can disable physical-activity sensing without losing gameplay. Battery saver automatically reduces wallpaper and explorer animation frequency.
- Automatic development debug APK compilation via GitHub Actions.

**Not yet implemented:** genuine continuous exploration, high-quality layered painted art, a persistent all-day step total, Google Play Services Activity Recognition transitions, automatic home/away detection, Health Connect, native smartwatch synchronization, complete seasonal calendars, Play Store production bundle and release signing. **v0.4 does not track continuously in the background**: the Android hardware step-counter listener runs only when Wisp is visible, and its output is limited to a recent walking hint rather than a saved step history. These are requirements or ideas, not shipped features.

## Universal Android direction and privacy

- **Proposed minimum: Android 10 (API 29)**, target/compile **Android 16 (API 36)** to prepare for current Play submission requirements. Older and non-Pixel phones must be actually tested.
- Wisp must work fully **without** step sensors, GPS access, a smartwatch, Health Connect or Google Play services. Optional connected data can enrich—not gate—the experience.
- Activity, home status, location/weather, quiet hours and watch integration are each independently consented to. No route history or indiscriminate background tracking.
- Respect battery saver, reduced motion, Android accessibility, and device/launcher-specific wallpaper behaviour.
- User controls over sensing, data deletion, manual context, memory export, and reversible evolution.

See **[ANDROID_PRODUCT_PLAN.md](ANDROID_PRODUCT_PLAN.md)** for the contextual intelligence feature matrix, privacy boundaries, platform compatibility and Play roadmap.

## Download development APKs

Go to [GitHub Actions](https://github.com/kalyps0tl/wisp-and-co-android/actions), open a successful run, and download the `WispAndCo-debug-apk` artifact. Extract the ZIP and install the APK with Android Files.

Only download from **successful** runs. These are **debug** builds for testing, not Play Store releases. Updating over old debug versions can fail if the original app used a different signing identity.

## Play Store readiness checklist

We will eventually need a privately secured release signing identity, a signed **Android App Bundle (AAB)**, Play Console account and identity verification, privacy policy, Play Data safety form, a deliberate permission UX, multiple-device tests, screenshot/store assets and potentially a qualifying closed testing period.

**Not suitable for production yet.** This project currently contains placeholder art and partly simulated world systems; an Android build passing does **not** mean the app has been validated on each device.

## Build

Android Gradle Plugin 8.13.2, Gradle 8.13, Java 17, Android SDK 36. Android 10 / API 29 minimum, pending testing across brands.
