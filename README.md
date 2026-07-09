# rerune-android

Public Android showcase repository for the ReRune Android SDK.

This repo demonstrates how to consume the published Maven Central artifacts in a
real Android showcase app.
It demonstrates the core OTA localization use case: the app can ship only
default app strings while ReRune delivers additional dashboard languages after
release.

The important ReRune OTA behavior shown here is remote-language delivery:
German is available from the dashboard payload even though the example apps do
not include app-owned `values-de/strings.xml` files. After the manifest and
locale payload are fetched, no new app-store build is needed just to add German.

## Artifacts

Compose:

```kotlin
implementation("io.rerune:rerune-android-compose:0.5.0")
```

Views:

```kotlin
implementation("io.rerune:rerune-android-views:0.5.0")
```

Both top-layer artifacts expose `io.rerune:rerune-android-core` transitively.

## Example apps

- `compose-example`: Jetpack Compose integration using native
  `stringResource(...)`
- `views-example`: classic Android Views integration using native XML and
  `getString(...)`

Both examples ship with a public demo `otaPublishId` that includes German
dashboard translations. The examples keep app string resources in the default
`values/` directory only, so switching a device to German exercises ReRune
remote-only language delivery instead of app-bundled German strings.

Override it locally if needed:

```bash
./gradlew :compose-example:assembleDebug -PRERUNE_OTA_PUBLISH_ID=replace-me
./gradlew :views-example:assembleDebug -PRERUNE_OTA_PUBLISH_ID=replace-me
```

or:

```bash
RERUNE_OTA_PUBLISH_ID=replace-me ./gradlew :compose-example:assembleDebug
```

## Compose quick start

```kotlin
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    ReRune.setup(
      app = this,
      otaPublishId = BuildConfig.RERUNE_OTA_PUBLISH_ID,
    )
  }
}
```

```kotlin
override fun attachBaseContext(newBase: Context) {
  super.attachBaseContext(newBase.reRune())
}
```

```kotlin
@Composable
fun ExampleScreen() {
  reRuneObserveRevision {
    Text(text = stringResource(R.string.title))
  }
}
```

## Views quick start

```kotlin
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    ReRune.setup(
      app = this,
      otaPublishId = BuildConfig.RERUNE_OTA_PUBLISH_ID,
    )
    ReRuneViews.install(this)
  }
}
```

```kotlin
override fun attachBaseContext(newBase: Context) {
  super.attachBaseContext(newBase.reRune())
}
```

```kotlin
titleText.text = getString(R.string.title)
```

## Remote-only language flow

ReRune Android SDK `0.5.0` can expose and apply locales that exist in the
ReRune dashboard even when the APK does not contain compiled
`values-<locale>` resources.

The dashboard manifest is the runtime source of truth for ReRune locales. The
SDK fetches every manifest locale it can reach, not only locales already present
in app resources.

System language following works without an in-app picker:

1. Install either example app.
2. Pull to refresh once so the app fetches the ReRune manifest and locale XML.
3. Set the Android system language to German.
4. Restart the app. Text resolves from cached ReRune German payloads and falls
   back to bundled English/default resources for missing keys.

Apps with a language picker can list dashboard locales:

```kotlin
lifecycleScope.launch {
  ReRune.availableLocalesFlow.collect { locales ->
    // locale.localeTag: "de", "pt-BR", ...
    // locale.isCached: payload is available offline right now
  }
}
```

And apply a remote-only locale explicitly:

```kotlin
ReRune.setLocaleOverride("de")
ReRune.setLocaleOverride(null) // follow Android system/context locale again
```

## Notes

- The SDK keeps bundled resources as the fallback safety net.
- New string resource keys still require an app release because Android
  `R.string.*` ids are compiled into the app.
- Compose redraw is opt-in through `reRuneObserveRevision { ... }`.
- Views redraw is app-owned; `reRuneOnStringsUpdated(...)` only notifies.
- OS-level language lists and app-store language metadata still come from the
  app/platform, not ReRune dashboard state.
- This repo is a showcase app repository.

## Run locally

```bash
./gradlew :compose-example:installDebug
./gradlew :views-example:installDebug
```

## License

MIT. See `LICENSE`.
