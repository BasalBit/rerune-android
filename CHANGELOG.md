# Changelog

## 0.1.3 - 2026-07-28

- Updated both examples to consume ReRune Android SDK `0.13.0`.
- Migrated locale display code to the SDK's context-based locale resolution.
- Replaced the MIT license with BasalBit GmbH's proprietary license.

## 0.1.2 - 2026-07-09

- Updated both examples to consume ReRune Android SDK `0.5.0`.
- Updated the public demo `otaPublishId` to the dashboard payload that includes
  remote German translations.
- Kept example app string resources in the default `values/` directory so
  additional app languages are resolved from ReRune.

## 0.1.1 - 2026-03-30

- Updated the showcase apps to compile and target Android SDK 36.
- Upgraded the showcase build to Android Gradle Plugin `9.0.1` and Gradle `9.1.0`.
- Migrated the repo to AGP built-in Kotlin support.
- Added the Compose Compiler Gradle plugin for the Compose example app.

## 0.1.0 - 2026-03-30

- Added the first public Android showcase repo for ReRune.
- Added `compose-example` using `io.rerune:rerune-android-compose`.
- Added `views-example` using `io.rerune:rerune-android-views`.
- Wired both examples to a public demo `otaPublishId` with local override
  support.
