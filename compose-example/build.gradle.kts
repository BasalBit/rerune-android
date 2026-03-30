import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

val otaPublishId = providers.gradleProperty("RERUNE_OTA_PUBLISH_ID")
  .orElse(providers.environmentVariable("RERUNE_OTA_PUBLISH_ID"))
  .orElse(providers.gradleProperty("RERUNE_DEMO_OTA_PUBLISH_ID"))
  .get()

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

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.14"
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
}

dependencies {
  implementation(libs.rerune.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.activity.compose)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.ktx)

  debugImplementation(libs.compose.ui.tooling)
}
