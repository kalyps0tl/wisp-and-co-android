# Wisp & Co. 🌿

**Phone-first Android live wallpaper and interactive companion.** A gentle, autonomous woodland spirit lives in a cozy world that changes with the weather and with the things it learns.

## What is now in this repository

This is the **first native Android development foundation**, not the finished app or an installation-tested release.

- Native `WallpaperService` with an animated Canvas landscape.
- Five visitable scenes: dock, garden, pond, forest and the **cottage interior**.
- Procedural bees, dragonflies, fireflies, ripples and a small animated Wisp (temporary character art).
- Visible weather label on the wallpaper and rain/snow/fog/storm render modes.
- Opt-in, approximate-location current weather from Open-Meteo, plus manual weather preview.
- Wisp's on-device activity and project progression, and petting interactions.
- A three-question matching quiz and meaningful woodland, antlered, moth and sprout forms.
- Optional celebration preferences saved in settings (the full activities are **not implemented yet**).
- GitHub Actions workflow that attempts to compile a **debug APK** automatically when you push to `main`.

## Download the first build, if the workflow passes

1. Open the **Actions** tab for this repository.
2. Open the latest **Build Wisp & Co Android Debug APK** run.
3. Confirm the result is green. Under **Artifacts**, download **WispAndCo-debug-apk**.
4. Extract the ZIP on your Pixel 10 Pro and install `app-debug.apk` (Android may ask to allow installs from your browser/files app).
5. Open **Wisp & Co.**, choose **Explore cottage, pond, garden and forest**, and then select **Set the living wallpaper**.
6. Preview rain/snow/storm in the app, then optionally grant approximate location permission for live local weather.

**Do not install an APK from an unsuccessful run.** Compilation and actual device behaviour have not yet been verified. The current scene changes are separate selectable locations, *not yet a seamless world with collision-aware walking*. The visual art is intentionally a temporary functional renderer; the soft storybook animation and sophisticated autonomy remain priorities.

## Long-term vision

- A truly explorable cottage interior, lake/pond, garden, forest, and little inhabitants with relationships and schedules.
- A beautifully restrained, layered storybook visual aesthetic rather than a static painting or floating ball.
- Wisp independently chooses projects, learns from new discoveries, asks for advice or comfort, remembers shared moments, and continues its life without neglect penalties.
- Substantial body-form customization, personality-matched beginnings, and **reversible appearance evolution** based on experience.
- Seasonal and culturally respectful opt-in activities including carving pumpkins, Christmas-tree decorating and Hanukkah traditions.
- One synchronized Wisp and world on the phone and eventually the Pixel Watch 2.

## Privacy

Do not commit personal locations, precise GPS logs, account keys or private data here. Location weather is explicitly opt-in; state is stored on-device. No secrets are required for the weather API.

## Development requirements

Android API 35, Java 17, Android Gradle Plugin 8.9.2, Gradle 8.12 (GitHub workflow), min SDK 31.
