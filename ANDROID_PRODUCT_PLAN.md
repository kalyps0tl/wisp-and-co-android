# Wisp & Co. — Native Android Product Plan

Updated 2026-10-30. This document replaces the earlier Pixel 10 Pro–specific assumption.

## Product definition

Wisp & Co. is a **native Android live wallpaper and companion app**, built for eventual Google Play distribution across compatible Android phones from multiple manufacturers. Wear OS is an **optional** companion, not a requirement and not necessarily tied to one watch brand.

One continuous Wisp identity and local world: each installation keeps the companion's relationships, memories, projects, evolving appearance, and optional seasonal celebrations. Users who decline any optional permission still get a fully playable, charming world.

## Platform scope and device behavior

- Phone: **Android 10 (API 29) or later** proposed baseline, with responsive UI across screen sizes/aspect ratios, not just Pixel 10 Pro. Validate compatibility on multiple manufacturers and Android OS releases before release.
- Android live wallpaper: WallpaperService on devices/launchers supporting it. App explorer remains fully functional if live wallpaper isn't supported.
- Activity sensors: hardware step counter if available; fallback to Activity Recognition and manual walk context. Never mark step sensors as required in the Play manifest. No invasive exercise tracking by default.
- Connected health data: optional Health Connect **read-only steps/activity** integration later, if useful and approved; never required for gameplay. Health Connect may not be available on every Android phone.
- Wear OS: optional companion module in a later milestone, pairing a shared Wisp state rather than duplicating or multiplying creatures. No assumption of a Pixel Watch.
- Offline: autonomous scene and journal work without a network connection; weather gracefully becomes last-known/manual.
- Battery: cap wallpaper FPS, render only while visible, cache scenic layers, batch contextual updates, and disable unnecessary refresh work when battery saver is on.

## Context engine: signals, permissions, effects, and fallbacks

| Signal | Intended source | Permission and trust boundary | Possible Wisp response | Fallback |
|---|---|---|---|---|
| Local time, weekday, time zone | Android system clock | No special permission | Sleep/awake, gardening schedule, firefly nights | Same |
| Weather, temperature | Open-Meteo and user-consented coarse location | Foreground approximate location, optional network request; never commit coordinates | Outfits, weather behavior, snow/rain, animal routines | Manual weather |
| Sunrise and sunset | Forecast or rough local solar time | Optional coarse location only | Morning mist, sunset glow, lanterns | Fixed clock window |
| Walking, still, cycling, vehicle | Android Activity Recognition Transition API | Optional ACTIVITY_RECOGNITION; Google Play services dependent | Independent outdoor wandering, resting, travel observations; **never interact while driving** | Manual walk mode |
| Steps | Health Connect (preferred aggregate) or device step counter, depending on availability | Optional explicit permissions; no compulsive streaks or fitness scores | Longer natural exploration may lead to discoveries | Simple time/activity estimate |
| Home/away | User-defined zone and optional event detection | **Do not request background location unless demonstrably core and approved by Play policy**; use manual Home/Away toggle initially | Cottage routines, setting off on adventure | Manual Home/Away |
| Charging, battery saver, thermal conditions | System APIs | Usually no special permission | Slower particle effects, indoor quiet time; lower FPS | Safe default |
| Connectivity | System status | Avoid scanning nearby networks / Wi-Fi SSIDs | Seamless offline simulation | Offline by default |
| Quiet hours / DND | User-selected in-app quiet schedule first | Avoid notification-listener or policy access solely for cosmetic effects | Wisp sends fewer alerts and waits quietly | In-app quiet schedule |
| Locale, calendar date | System locale/time | No personal calendars needed | Local-language UI, seasonal changes and event calendar | Manual event selection |
| Seasonal/cultural events | Explicit user choices; accurate holiday calendars | No inference of religion, ethnicity or identity | Pumpkin carving, Christmas decorations, Hanukkah activities, etc. | Opt out |
| Accessibility/reduced motion | Android settings and in-app controls | No special permissions | Static/low-motion visual mode, readable controls, no intense flashes | Safe defaults |
| In-game history, user choices | App's private local state | On-device storage, optional export | Personality changes, growth, relationships, project choices | Same |
| Optional watch sync | Wear OS Data Layer / Health Connect where applicable | Paired watch/app and explicit sync/fitness consent | Shared Wisp view/activity | Phone-only |

### Important boundaries

- Context should **influence**, never enslave, the creature. Wisp is autonomous and should sometimes do things unrelated to the user's motions.
- No GPS route history, nearby Wi-Fi scans, microphone listening, camera monitoring, messages, browsing history, contacts, or other invasive signals.
- No inferring sensitive health, religion, emotional state, disability, or identity from sensors. A quiet phone does not mean the user is sad; no steps does not mean the user is inactive.
- Users can view and override current context, grant/withdraw permissions, pause sensing, delete all personal data, and export their evolving Wisp.
- Permissions are requested contextually, never in a permission wall at onboarding.
- Seasonal rituals are opt-in, culturally respectful, date-correct, and do not trivialize religious observance.
- Wisp never dies or punishes users for low movement, travel, not carrying the phone, or inactivity.

## Privacy and battery testing

Create a clear on-device "Wisp senses" panel with toggles for time, approximate weather, movement, optional home status, and optional connected steps. Show last-updated time and reason each context affected Wisp. Never imply automatic always-on tracking when platform permissions or lifecycle do not permit it.

Instrument frame time, dropped frames, battery drain, weather refresh/network frequency, and fallback functionality on at least:
- Android 10/11 older or midrange hardware
- Android 12/13
- Android 14/15
- Android 16 and later
- Different launcher and aspect ratio implementations (Samsung, Google, Motorola, etc.)

## Play Store publication requirements / roadmap

- As of **August 31, 2026**, **new Play app submissions target Android 16 / API 36 or newer** (verify Google Play requirements at release).
- Prefer **Android App Bundle (AAB)** for Play delivery; keep debug APK builds for sideload testing.
- Use a private **release signing** key and Google Play App Signing. Never use a debug/cache signing key for the release.
- Add privacy policy, Play Data safety form, and permissions explanation for any Activity Recognition, Health Connect, or location access. Background location carries additional review and may not be appropriate.
- Support user-managed save export/restore and deletion; test upgrade/migration of persistent world state.
- Personal developer registration is currently **US$25 one-time**, with identity verification; new personal developer accounts have a closed-testing requirement before public production access.
- No ad SDKs, third-party trackers, or real-money virtual currency needed. No pressure loops or guilt-based notifications.
- Build a release signing and bundle flow only after release credentials are safely established outside Git.

## Next implementation milestones

1. Full native Android API 36 build and compatibility smoke tests.
2. An optional, permission-light **Context Engine** with time, battery saver, manual Home/Away and walking modes first; add actual Activity Recognition later.
3. Render world and Wisp independently with consistent coordinates, transition animation, and a proper interaction/navigation graph.
4. Hand-painted layered backgrounds and independent animated Wisp sprite/rig, cottage furnishing and wildlife schedules.
5. Memory-driven autonomous activities, reversible evolution timeline, accessible settings and clean save migration.
6. Contextual sensor integrations and explicit permission UX, ensuring no damage to battery or Play review viability.
7. Wear OS companion as separate optional module.
8. Accessibility, localization, tests, Play closed testing, private release signature, AAB, store listing.

## Reality check

The currently compiled v0.3 has no step counter, no Activity Recognition transitions, no home geofence, no Health Connect, no Wear OS integration, and no Play release bundle. These are plans, not shipped capabilities. 
