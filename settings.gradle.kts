rootProject.name = "brreg-client"

pluginManagement {
    val jvmVersion: String by settings
    plugins {
        kotlin("jvm") version jvmVersion
        kotlin("plugin.serialization") version jvmVersion
        id("org.jmailen.kotlinter")
        id("maven-publish")
    }
}
