import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

val otaPublishId = providers.gradleProperty("RERUNE_OTA_PUBLISH_ID").get()

android {
  namespace = "io.rerune.example.compose"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.rerune.example.compose"
    minSdk = 21
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    buildConfigField("String", "RERUNE_OTA_PUBLISH_ID", "\"$otaPublishId\"")
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    resources {
      // Core and Compose both ship their license. Preserve every notice in the APK.
      excludes -= "/META-INF/LICENSE"
      merges += "META-INF/LICENSE"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_17
    }
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(libs.rerune.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.activity.compose)
  implementation(libs.appcompat)
  implementation(libs.compose.ui)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.lifecycle.viewmodel.ktx)

  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.compose.ui.test.junit4)
  debugImplementation(libs.compose.ui.test.manifest)
}
