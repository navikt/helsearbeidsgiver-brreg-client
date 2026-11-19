plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.kotest")
    id("org.jmailen.kotlinter")
    id("maven-publish")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenNav("*")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenNav("helsearbeidsgiver-${rootProject.name}")
    }
}

dependencies {
    val kotestVersion = project.property("kotestVersion") as String
    val kotlinxCoroutinesVersion = project.property("kotlinxCoroutinesVersion") as String
    val kotlinxSerializationVersion = project.property("kotlinxSerializationVersion") as String
    val ktorVersion = project.property("ktorVersion") as String
    val mockkVersion = project.property("mockkVersion") as String
    val slf4jVersion = project.property("slf4jVersion") as String
    val utilsVersion = project.property("utilsVersion") as String

    api("no.nav.helsearbeidsgiver:utils:$utilsVersion")

    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation(testFixtures("no.nav.helsearbeidsgiver:utils:$utilsVersion"))
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
}

fun RepositoryHandler.mavenNav(repo: String): MavenArtifactRepository {
    val githubPassword = project.property("githubPassword") as String

    return maven {
        setUrl("https://maven.pkg.github.com/navikt/$repo")
        credentials {
            username = "x-access-token"
            password = githubPassword
        }
    }
}
