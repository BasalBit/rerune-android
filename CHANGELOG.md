# Changelog

## Unreleased

- Added the reviewed Chapter reading experience to both ReRune examples, preserving their name, launcher icons, and Compose and native Views implementations.
- Added native fallback resources in five languages, chapter progress, bookmarks, discovery, language selection, and persistent Main/VIP selection.
- Added a session scoped Staging mode switch to both examples, with fallback copy in five languages. The samples do not enable staging during setup, so restarts return to published delivery.
- Updated Maven dependencies to published SDK `1.2.0` and the public demo OTA target. Live delivery now accepts its zero-based placeholder orders.
- Added model, UI, and opt-in live delivery checks.
- Fixed the Views example bottom navigation drawing icons and labels on top of each other: the navigation bar inset was applied twice inside the fixed bar height.
- Updated both examples to ReRune Android SDK `1.3.0`, which loads the manifest from the CDN with the publish ID in the URL path and requests locale versions through the URL path.
- Removed the `META-INF/LICENSE` merge rule. SDK `1.3.0` ships each notice at `META-INF/rerune/<artifactId>/LICENSE.txt`, which default packaging keeps.
- Updated both examples to ReRune Android SDK `1.4.0`. The SDK now uses the platform `HttpURLConnection` instead of OkHttp, so the examples no longer receive OkHttp and Okio through ReRune. SDK requests always reach the CDN and identify themselves as `rerune-android/1.4.0`. No example code changed.
- Updated both examples to ReRune Android SDK `1.5.0`. A downloaded language version whose texts need format arguments the app does not pass is now rejected as a whole, and the last accepted version stays in use. For a key without an OTA value in the phone's language, the app's own translation now comes before the main language OTA text, and format arguments follow the phone's language. No example code changed.
- Upgraded the example build to Android Gradle Plugin `9.4.1` and Gradle `9.8.1`, matching the SDK build. The `kotlin` compiler options block moved out of `android { }` to the top level. AGP `9.4.1` requires Gradle `9.6.0` or newer.
- CI verifies live locale downloads, Main/VIP headings, native formatting, and a language available only through OTA.
- Preserved merged SDK MIT notices and both bundled font licenses.
- Removed migration records, reference images, screenshot generation, copied ARB catalogs, and their generator and comparison tests. The running apps are the design reference; behavior tests and standalone Maven Central consumption remain.

## 0.1.5 (2026-08-06)

- Updated both examples to consume ReRune Android SDK `1.0.1`.

## 0.1.4 (2026-07-28)

- Updated both examples to consume ReRune Android SDK `0.14.0`.
- Restored the MIT license in line with the SDK release.

## 0.1.3 (2026-07-28)

- Updated both examples to consume ReRune Android SDK `0.13.0`.
- Migrated locale display code to the SDK's context-based locale resolution.
- Replaced the MIT license with BasalBit GmbH's proprietary license.

## 0.1.2 (2026-07-09)

- Updated both examples to consume ReRune Android SDK `0.5.0`.
- Updated the public demo `otaPublishId` to the dashboard payload that includes
  remote German translations.
- Kept example app string resources in the default `values/` directory so
  additional app languages are resolved from ReRune.

## 0.1.1 (2026-03-30)

- Updated the showcase apps to compile and target Android SDK 36.
- Upgraded the showcase build to Android Gradle Plugin `9.0.1` and Gradle `9.1.0`.
- Migrated the repo to AGP built-in Kotlin support.
- Added the Compose Compiler Gradle plugin for the Compose example app.

## 0.1.0 (2026-03-30)

- Added the first public Android showcase repo for ReRune.
- Added `compose-example` using `io.rerune:rerune-android-compose`.
- Added `views-example` using `io.rerune:rerune-android-views`.
- Wired both examples to a public demo `otaPublishId` with local override
  support.
