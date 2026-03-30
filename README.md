# rerune-android

Public Android showcase repository for the ReRune Android SDK.

This repo demonstrates how to consume the published Maven Central artifacts in a
real Android app without depending on the SDK source repository.

## Artifacts

Compose:

```kotlin
implementation("io.rerune:rerune-android-compose:0.4.1")
```

Views:

```kotlin
implementation("io.rerune:rerune-android-views:0.4.1")
```

Both top-layer artifacts expose `io.rerune:rerune-android-core` transitively.

## Example apps

- `compose-example`: Jetpack Compose integration using native
  `stringResource(...)`
- `views-example`: classic Android Views integration using native XML and
  `getString(...)`

Both examples ship with a public demo `otaPublishId` so the repo works out of
the box.

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

## Notes

- The SDK keeps bundled resources as the fallback safety net.
- Compose redraw is opt-in through `reRuneObserveRevision { ... }`.
- Views redraw is app-owned; `reRuneOnStringsUpdated(...)` only notifies.
- This repo is a consumer showcase. For SDK source, releases, and internal
  implementation details, see `https://github.com/BasalBit/rerune-android-ota`.

## Run locally

```bash
./gradlew :compose-example:installDebug
./gradlew :views-example:installDebug
```

## License

MIT. See `LICENSE`.
