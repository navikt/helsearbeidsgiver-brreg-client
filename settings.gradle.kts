rootProject.name = "brreg-client"

pluginManagement {
    val kotestVersion = providers.gradleProperty("kotestVersion").get()
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val kotlinterVersion = providers.gradleProperty("kotlinterVersion").get()

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("io.kotest") version kotestVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("maven-publish")
    }
}
