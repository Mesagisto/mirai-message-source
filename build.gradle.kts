import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    mavenCentral()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:4.0.2")
  }
}

plugins {
  java
  kotlin("jvm") version "1.5.10"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  kotlin("plugin.serialization") version "1.5.10"
  id("net.mamoe.mirai-console") version "2.8.0-M1"
}

allprojects {
  group = "org.meowcat"
  version = "1.0.0"
  tasks {
    withType<KotlinCompile>().all {
      kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
      }
      sourceCompatibility = "11"
    }
  }
  repositories {
    mavenCentral()
    maven("https://jitpack.io")
    google()
    mavenLocal()
  }
}
