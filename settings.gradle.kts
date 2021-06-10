pluginManagement {
   repositories {
      gradlePluginPortal()
      mavenCentral()
      google()
   }
}

rootProject.name = "mirai-mesaga-fonto"
include(":plugin")
when (System.getenv("BUILD_ANDROID")) {
   "true" -> {
      include(":android")
   }
}
