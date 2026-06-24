plugins {
    alias(libs.plugins.shadow)
    java
}

group = "de.chojo"
version = "1.1.0"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
    mavenCentral()
}

dependencies {
    implementation(project(":plugin-api"))

    // discord
    implementation("de.chojo", "cjda-util", "2.14.5+jda-6.3.0") {
        exclude(module = "opus-java")
    }

    implementation("io.github.kaktushose:jda-commands:5.0.0")

    implementation("net.dv8tion", "JDA", "6.4.1")

    implementation(libs.javalin.bundle)
    implementation(libs.javalin.openapi)
    implementation(libs.javalin.swagger)
    annotationProcessor(libs.javalin.annotation)
    implementation("net.lingala.zip4j", "zip4j", "2.11.6")

    // config
    implementation("dev.chojo", "ocular", "2.2.1")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.1.4")

    // database
    implementation(libs.bundles.sadu)
    implementation("org.postgresql", "postgresql", "42.7.11")

    // docker api
    implementation(libs.bundles.docker)

    // Logging
    implementation(libs.bundles.logging)
    implementation("club.minnced", "discord-webhooks", "0.8.4")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter", "junit-jupiter")
}

tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                expand(
                    "version" to project.version
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.gamejam.Bot"))
        }
    }
}
