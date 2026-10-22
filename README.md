# ReRune Android examples

Standalone native Android apps for trying the published ReRune SDK. Clone this
repository and open it in Android Studio, or build with the included Gradle
wrapper. No ReRune source checkout, local SDK build, or Maven Local setup is
required.

| Example | Published dependency |
| --- | --- |
| `compose-example` | `io.rerune:rerune-android-compose:1.5.0` |
| `views-example` | `io.rerune:rerune-android-views:1.5.0` |

Both resolve from Maven Central. Versions are pinned in
[the version catalog](gradle/libs.versions.toml). These are Kotlin Android apps;
the Flutter package on pub.dev is not used here.

The running Compose and native Views apps are the design reference. Review UI
changes in the apps; this repository keeps no reference screenshots or golden
images.

## Build and run

With Android Studio installed:

1. Open this repository's root folder and let Gradle sync.
2. Select `compose-example` or `views-example` and a device or emulator.
3. Click **Run**.

The public demo is already configured. No account, API key, ReRune source,
resource generation, or configuration changes are needed to try it. Gradle
downloads the published SDK from Maven Central. Android Studio may prompt you
to install Android SDK platform 36 and build tools on first use.

For command-line builds, use JDK 17 and Android SDK platform 36. Set `JAVA_HOME`
and either set `ANDROID_HOME` or configure `sdk.dir` in an untracked
`local.properties`.

Run from the repository root:

```bash
./gradlew :compose-example:assembleDebug :views-example:assembleDebug
```

With an Android device or emulator connected:

```bash
./gradlew :compose-example:installDebug
./gradlew :views-example:installDebug
```

Both apps are named ReRune, retain the ReRune icon, and support Android API 21
and newer. Release assemblies are unsigned.

## Try ReRune

The reading apps include three stories, chapter progress, bookmarks, discovery
filters, and a language picker. Reading state lasts for the app session and
survives locale changes. Reading settings offer persistent Main/VIP selection,
a session scoped Staging mode switch, and a refresh action.

Toggle VIP in reading settings, then close the sheet to see the library heading
change. Choose French or Albanian to try a language supplied entirely by ReRune.

Turn on Staging mode to preview enabled draft translations before publication.
The samples do not enable staging in `ReRune.setup(...)`, so restarting either
app returns it to published delivery.

Both apps pass the public OTA ID directly to `ReRune.setup(...)`:

```text
03141fc5dde6e5a1f9debf99ee68bbb125dc830412fdfb85af4834d3de341b3b
```

SDK `1.5.0` supports the public target's placeholder format. With SDK `1.4.0`,
both examples successfully downloaded all seven published locales and reported
an unchanged second refresh on October 20, 2026. English, German, Spanish, Italian, and
Portuguese are also bundled for offline fallback.

## Integration code

Each `DemoApplication` passes the public OTA ID directly to `ReRune.setup(...)`.
Views also calls `ReRuneViews.install(this)`.
Both activities wrap their base context with `newBase.reRune()` and use native
string and plural resources.

Compose observes updates with `reRuneObserveRevision { ... }`. Views uses
`reRuneOnStringsUpdated(lifecycleScope) { ... }` to rebind existing widgets.
The settings dialog stays attached during a text revision.

`Storybook.kt` contains the app's reading state and delegates refresh and variant
selection to `ReRune.checkForUpdates()`, `ReRune.getVariant()`, and
`ReRune.setVariant(variant, persist = true)`. It switches delivery mode with
`ReRune.setStaging(...)`. A non-null SDK error message is reported as failure
even when the SDK status is `Updated` or `NoChange`.

## Tests

```bash
./gradlew :compose-example:testDebugUnitTest :views-example:testDebugUnitTest
./gradlew :compose-example:lint :views-example:lint
```

Model and Robolectric UI tests cover reading state, locale changes,
and Main/VIP revisions. Test-only cache fixtures use the published SDK's public
interfaces. They do not demonstrate live delivery. Tests assert behavior without
writing screenshots. Reports go under each module's ignored `build/reports/` directory.

To check real delivery through the published SDK and configured OTA ID:

```bash
./gradlew :compose-example:testDebugUnitTest :views-example:testDebugUnitTest -Prerune.liveDelivery=true --continue
```

This opt-in check writes `build/reports/rerune-delivery.txt` in each module.
It requires network access and expects an initial update followed by an
unchanged refresh. It also checks live Main/VIP headings, native placeholder
formatting, and French text that is not bundled in either app.

## Maintaining bundled translations

Both apps commit native Android fallback resources for English, German, Spanish,
Italian, and Portuguese under `src/main/res/values*/strings.xml`. ReRune supplies
OTA text. Preserve existing translation keys, native plural quantities, and typed
positional arguments when updating the bundled XML. No Python generator or
separate catalog copy is required.

## Licenses

Each APK keeps the Core and UI SDK MIT notices at
`META-INF/rerune/<artifactId>/LICENSE.txt`; no packaging rule is needed.
Instrument Sans and Lora font licenses are bundled in each app's
`src/main/assets/licenses/`. Repository license: [MIT](LICENSE).
