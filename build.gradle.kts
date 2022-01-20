import shadow.org.apache.commons.io.FilenameUtils
plugins {
  java
  kotlin("jvm") version "1.5.31"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  kotlin("plugin.serialization") version "1.5.31"
  id("net.mamoe.mirai-console") version "2.9.2"
}
group = "org.meowcat"
version = "1.1.1"
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
  mavenLocal()
}
mirai {
  coreVersion = "2.9.2"
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
  compileOnly("io.nats:jnats:2.13.1")
  implementation("org.rocksdb:rocksdbjni:6.27.3")
  implementation("com.github.gotson:webp-imageio:0.2.2")

  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.2.2")
  compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.2")
  implementation("org.meowcat:mesagisto-client-jvm:1.1.2")
  // implementation("org.meowcat:mesagisto-client:1.0.18")
  testCompileOnly("junit:junit:4.13.2")
}
