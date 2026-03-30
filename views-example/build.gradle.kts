plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

val otaPublishId = providers.gradleProperty("RERUNE_OTA_PUBLISH_ID")
  .orElse(providers.environmentVariable("RERUNE_OTA_PUBLISH_ID"))
  .orElse(providers.gradleProperty("RERUNE_DEMO_OTA_PUBLISH_ID"))
  .get()

android {
  namespace = "io.rerune.example.views"
  compileSdk = 34

  defaultConfig {
    applicationId = "io.rerune.example.views"
    minSdk = 21
    targetSdk = 34
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

  kotlinOptions {
    jvmTarget = "17"
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
