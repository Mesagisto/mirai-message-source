pluginManagement {
   repositories {
      gradlePluginPortal()
      mavenCentral()
      jcenter()
      google()
      maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
      maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
      maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
      maven(url = "https://kotlin.bintray.com/kotlinx")
   }
}

rootProject.name = "mirai-mesaga-fonto"
include(":plugin")
when (System.getenv("BUILD_ANDROID")) {
   "true" -> {
      include(":android")
   }
}
