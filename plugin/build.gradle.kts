plugins {
  kotlin("jvm")
  java
  id("com.github.johnrengelman.shadow")
  kotlin("plugin.serialization")
  id("net.mamoe.mirai-console")
}
repositories {
  maven("https://jitpack.io")
  mavenCentral()
}

mirai {
  coreVersion = "2.8.0-M1"
  jvmTarget = JavaVersion.VERSION_11
  excludeDependency("org.jetbrains.kotlin:kotlin-stdlib")
  excludeDependency("org.jetbrains.kotlin:kotlin-reflect")
  excludeDependency("org.jetbrains.kotlin:kotlin-stdlib-common")
  excludeDependency("org.jetbrains:annotations")
}
dependencies {
  implementation("io.arrow-kt:arrow-core:1.0.0")
  implementation("io.nats:jnats:2.12.0")
  implementation("org.rocksdb:rocksdbjni:6.22.1.1")
  implementation("com.github.gotson:webp-imageio:0.2.2")
  implementation("org.tinylog:tinylog-impl:2.4.0-M1")
  implementation("org.tinylog:tinylog-api-kotlin:2.4.0-M1") {
    isTransitive = false
  }
  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.0")
  implementation("org.meowcat:mesagisto-client-jvm:1.0.2")

  testCompileOnly("junit:junit:4.13.2")
}
