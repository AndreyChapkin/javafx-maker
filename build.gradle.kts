import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.homework"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.controls", "javafx.web")
}

application {
    mainClass.set("org.my.MainKt")
}

dependencies {
    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    // kotlin reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}