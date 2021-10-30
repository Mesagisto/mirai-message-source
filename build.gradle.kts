import shadow.org.apache.commons.io.FilenameUtils
plugins {
  java
  kotlin("jvm") version "1.5.21"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  kotlin("plugin.serialization") version "1.5.21"
  id("net.mamoe.mirai-console") version "2.8.0-M1"
}
group = "org.meowcat"
version = "1.1.0-rc1"
tasks.compileKotlin {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
  }
  sourceCompatibility = "1.8"
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  google()
  // mavenLocal()
}
mirai {
  coreVersion = "2.8.0-M1"
  jvmTarget = JavaVersion.VERSION_1_8
  excludeDependency("org.jetbrains.kotlin:kotlin-stdlib")
  excludeDependency("org.jetbrains.kotlin:kotlin-reflect")
  excludeDependency("org.jetbrains.kotlin:kotlin-stdlib-common")
  excludeDependency("org.jetbrains:annotations")

  configureShadow {
    exclude { file ->
      val excludeFiles = arrayOf(
        "kotlin/*",
        "kotlinx/coroutines/*",
        "kotlinx/serialization/*",
        "org/bouncycastle/*"
      )
      val includeFiles = arrayOf(
        "kotlinx/serialization/cbor/*"
      )
      var shouldExclude = false

      excludeFiles.forEach first@{ excludeFile ->
        if (FilenameUtils.wildcardMatch(file.path, excludeFile)) {
          shouldExclude = true
          includeFiles.forEach second@{ includeFile ->
            if (FilenameUtils.wildcardMatch(file.path, includeFile)) {
              shouldExclude = false
              return@second
            }
          }
          return@first
        }
      }
      shouldExclude
    }
    minimize()
  }
}
dependencies {
  implementation("io.arrow-kt:arrow-core:1.0.0")
  implementation("io.nats:jnats:2.12.0")
  implementation("org.rocksdb:rocksdbjni:6.22.1.1")
  implementation("com.github.gotson:webp-imageio:0.2.2")

  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.0")
  implementation("org.meowcat:mesagisto-client-jvm:1.0.9")

  testCompileOnly("junit:junit:4.13.2")
}
