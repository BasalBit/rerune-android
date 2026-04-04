import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
}

val otaPublishId = providers.gradleProperty("RERUNE_OTA_PUBLISH_ID")
  .orElse("a68e482cde9e205aae1a4249ed56972d5eb95b34ea3451a8c5f7cf18b9da0d2a")
  .get()

android {
  namespace = "io.rerune.example.views"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.rerune.example.views"
    minSdk = 21
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    buildConfigField("String", "RERUNE_OTA_PUBLISH_ID", "\"$otaPublishId\"")
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
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
  implementation(libs.rerune.views)

  implementation(libs.core.ktx)
  implementation(libs.appcompat)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.swipe.refresh.layout)
  implementation(libs.material)
}
