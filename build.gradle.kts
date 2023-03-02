import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.5.20"

    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("me.qoomon.git-versioning") version "5.1.1"
}

group = "network.warzone.mars"

val majorVersion = "1.0-SNAPSHOT"
version = majorVersion

gitVersioning.apply {
    refs {
        branch(".+") {
            version = "${project.version}-\${ref}-\${commit.short}"
        }
    }

    // fallback configuration in case of no matching ref configuration
    rev {
        version = "${project.version}-\${commit.short}"
    }
}

repositories {
    mavenCentral()

    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }

    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.pkg.github.com/PGMDev/PGM")
    maven("https://repo.aikar.co/nexus/content/groups/aikar/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://m2.dv8tion.net/releases/")
    maven("https://nexus.scarsz.me/content/groups/public")
}

dependencies {
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    //implementation("com.google.code.gson:gson:2.8.2")

    implementation("net.time4j:time4j-base:5.8")
    implementation("net.time4j:time4j-sqlxml:5.8")
    implementation("net.time4j:time4j-tzdata:5.0-2020a")

    compileOnly("app.ashcon:sportpaper:1.8.8-R0.1-SNAPSHOT")
    implementation("app.ashcon.intake:intake-bukkit:1.3-SNAPSHOT")

    compileOnly("tc.oc.pgm:core:0.14-SNAPSHOT") {
        exclude(group = "fr.mrmicky", module = "FastBoard")
    }
    compileOnly("fr.mrmicky:fastboard:1.2.0")

    compileOnly("com.discordsrv:discordsrv:1.24.0")

    compileOnly("net.kyori:adventure-api:4.8.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.8.1")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")


//    compileOnly("me.lucko:adventure-platform-bukkit:4.0.0") {
//        exclude(group = "org.spigotmc", module = "spigot-api")
//    }


    val ktorVersion = "1.6.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation("com.tinder.scarlet:scarlet:0.1.12")
    implementation("com.tinder.scarlet:websocket-okhttp:0.1.12")
    implementation("com.tinder.scarlet:message-adapter-gson:0.1.12")
    implementation("com.tinder.scarlet:stream-adapter-rxjava2:0.1.12")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.kittinunf.result:result-jvm:5.1.0")
}

tasks.withType<ShadowJar> {
//    minimize()
    manifest {
        attributes["Main-Class"] = "network.warzone.mars.MarsKt"
    }

    archiveBaseName.set("Mars")
    archiveVersion.set("$majorVersion")

    filesMatching("**/plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
//    dependsOn(project.tasks.shadowJar)
}
