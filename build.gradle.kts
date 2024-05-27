import org.gradle.jvm.tasks.Jar

plugins {
    application
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "net.collectn"

application {
    mainClass.set("net.collectn.tools.movies.ApplicationKt")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier = "standalone"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes("Main-Class" to application.mainClass)
        }
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } + sourcesMain.output
        from(contents)
    }

    build {
        dependsOn(fatJar)
    }

    assemble {
        dependsOn(fatJar)
    }

    test {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("uk.co.conoregan:themoviedbapi:1.15.1")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

spotless {
    kotlin {
        ktlint("1.2.1")
    }
}
