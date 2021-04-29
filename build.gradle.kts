buildscript {
   repositories {
      google()
      jcenter()
      mavenCentral()
      maven("https://jitpack.io")
   }
   dependencies {
      classpath("com.android.tools.build:gradle:4.0.2")
      classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
   }
}

plugins {
   java
   kotlin("jvm") version "1.4.30"
   id("com.github.johnrengelman.shadow") version "5.2.0"
   kotlin("plugin.serialization") version "1.4.30"
   id("net.mamoe.mirai-console") version "2.5.0"
}

group = "org.meowcat"
version = "1.0.0"
allprojects {
   tasks {
      withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
         kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
         }
         sourceCompatibility = "1.8"
      }
   }
   repositories {
      mavenCentral()
      jcenter()
      google()
      maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
      maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
      maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
      maven(url = "https://kotlin.bintray.com/kotlinx")
      maven("https://jitpack.io")
   }
}
