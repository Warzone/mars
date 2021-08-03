import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.5.20"

    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "network.warzone.pgm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.ashcon.app/nexus/content/repositories/snapshots/")
    maven("https://repo.aikar.co/nexus/content/groups/aikar/")
}

dependencies {
    implementation("app.ashcon:sportpaper:1.8.8-R0.1-SNAPSHOT")
    implementation("app.ashcon.intake:intake-bukkit:1.2-SNAPSHOT")

    implementation("tc.oc.pgm:core:0.12-SNAPSHOT") {
        exclude(group = "fr.mrmicky", module = "FastBoard")
    }
    compileOnly("fr.mrmicky:fastboard:1.2.0")

    compileOnly("net.kyori:adventure-api:4.8.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.8.1")
    compileOnly("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    val ktorVersion = "1.6.1"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
}

tasks.withType<ShadowJar> {
    minimize()
    archiveClassifier.set("")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}