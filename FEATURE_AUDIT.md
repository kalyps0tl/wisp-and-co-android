# Wisp & Co. — Feature audit and production backlog

Checked against Android source at the v0.5 matching-onboarding milestone (October 2026). "Shipped" means implemented in debug source, not certified on all devices.

## Available in debug preview

- [x] Native Android companion launcher and optional live wallpaper
- [x] Five selectable settings/scenes: dock, garden, pond, forest, cottage interior
- [x] Procedurally drawn environments with animated Wisp, bees, dragonflies, pond ripples and basic seasonal decorations
- [x] Simple tap-to-move horizontal animation and choice-based discoveries
- [x] Wisp on-device identity, journal, independently chosen activity/project progress, opt-in celebrations and reversible appearance stage
- [x] Approximate-location weather opt-in, with manual preview and cached fallback
- [x] Context panel: clock, battery saver, manual home/away, manual walking/resting and optional step-event hint while visible
- [x] First-run *Meet Your Wisp* ceremony, six story questions, four matching dispositions, spirit appearance choice, optional name, safe confirmation
- [x] Android debug APK CI, Android 10+ minimum configuration, API 36 target

## Top quality and gameplay priorities

### P0 — visual identity and world presentation
- [ ] Commission or create **layered, authored storybook scenery** in coherent art direction: original backgrounds, middlegrounds, character, furniture, creatures and separate foreground occluders
- [ ] Rebuild drawing with aspect-preserving, performance-limited layers rather than stretched 400×900 scene
- [ ] Character animation rig/sprite sheets: idle, breathe, blink, walk, sit, react, carry objects, sleep; expressive ears/wings/antlers/sprout distinctions, not merely coloured oval
- [ ] Real map/interior navigation between rooms and outdoor landmarks, directional transitions, interactive hotspots and world-coordinate occlusion
- [ ] In-world menu and onboarding transitions, not a long scroll of system-style buttons

### P1 — real life and autonomous behaviour
- [ ] Time/season-dependent visits and believable resident wildlife cycles (bees, dragonflies, pond frog, fireflies)
- [ ] Activity and relationship planner with needs/preferences, memory, occasional surprise discoveries, independent interests and explicit advice/comfort requests
- [ ] Context explanations that show when sensor/weather/time affected a decision, with user overrides and history privacy controls
- [ ] Optional low-battery/thermal guardrails and measured render FPS/battery drain on representative Android devices
- [ ] Optional Activity Recognition transitions for better foreground movement detection; no background location by default
- [ ] Reliable manual home/away, and only later a consented location-independent or policy-compliant home status
- [ ] Reversible full appearance history rather than a small set of additive markings; held look remains preserved
- [ ] Weather/daylight seasons and outdoor conditions reflected consistently in wildlife, lighting, shelter and character behaviour

### P2 — quality, safety and release
- [ ] Private save export/restore, migration tests, deletion controls, and optional secure sync strategy
- [ ] Accessibility: TalkBack, contrast, motion reduction, scalable text, one-handed navigation
- [ ] Localization and date-correct, opt-in holiday calendars with careful culturally respectful representations
- [ ] Complete manual and automated UI tests for onboarding, sensor absence/permission denial, offline, weather, wallpaper/launcher variants, and updates
- [ ] Optional Wear OS companion module with synchronization, independent of one watch maker
- [ ] Play privacy policy, Data safety, permission declarations, release AAB and secure signing key, Play internal/closed testing

## Scope guardrails

- Storybook-looking Wisp is **not** automatically a child-directed app; intended audience and Play Families requirements need a deliberate decision.
- No starvation, streaks, forced attention, negative consequences for low steps, or guilt if the user doesn't open the app.
- Wisp owns its whims and projects; it should not copy the user's every movement.
- No passive microphone/camera monitoring, GPS path history, contacts, messages, intrusive app usage reading or hidden background tracking.
- Do not ship a fancy static wallpaper as the completed explorable app.
- New functionality is only validated after device testing, not simply after GitHub Actions assembles an APK.

## Next tangible development slice after v0.5

Build the visual/art pipeline (scene layers and character sprites, image asset resources, animation state machine) and a real first explorable cottage environment, while preserving Wisp's persistent personality and sensor settings.
