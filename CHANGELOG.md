# Changelog

## 0.1.2 - 2026-07-09

- Updated both examples to consume ReRune Android SDK `0.5.0`.
- Updated the public demo `otaPublishId` to the dashboard payload that includes
  remote German translations.
- Packaged only English/default resources so additional languages are resolved from ReRune.

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
