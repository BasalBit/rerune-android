import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "io.rerune.example.compose"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.rerune.example.compose"
    minSdk = 21
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
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
