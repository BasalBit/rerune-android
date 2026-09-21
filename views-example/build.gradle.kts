import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "io.rerune.example.views"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.rerune.example.views"
    minSdk = 21
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    viewBinding = true
  }

  packaging {
    resources {
      // Core and Views both ship their license. Preserve every notice in the APK.
      excludes -= "/META-INF/LICENSE"
      merges += "META-INF/LICENSE"
    }
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
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
  implementation(libs.lifecycle.viewmodel.ktx)
  implementation(libs.material)

  testImplementation(libs.junit)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.core)
}
