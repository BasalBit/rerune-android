import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
}

val otaPublishId = providers.gradleProperty("RERUNE_OTA_PUBLISH_ID")
  .orElse("22b014db4e8a26ccc1038e6d969fc663ccf90d2931f10ff4fbd7937b1ba69732")
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
  androidResources {
    localeFilters += listOf("en")
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
