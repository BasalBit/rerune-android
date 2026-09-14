plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
}

subprojects {
  tasks.withType<Test>().configureEach {
    val liveDelivery = providers.gradleProperty("rerune.liveDelivery").getOrElse("false")
    systemProperty("rerune.liveDelivery", liveDelivery)
    if (liveDelivery == "true") filter.includeTestsMatching("*ChapterDeliveryTest")
    systemProperty("example.reports", layout.buildDirectory.dir("reports").get().asFile.absolutePath)
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
  }
}
