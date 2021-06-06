plugins {
   kotlin("jvm")
   java
   id("com.github.johnrengelman.shadow")
   id("net.mamoe.mirai-console")
}
mirai {
   coreVersion = "2.6.2" // mirai-core version
}
dependencies {
   implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")
   implementation("org.meowcat:handy-dandy:0.1.0")

   implementation("io.nats:jnats:2.10.0")

   // jackson
   implementation("com.fasterxml.jackson.core:jackson-core:2.12.1")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
   implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.1")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
}
