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
    implementation("de.chojo", "cjda-util", "2.9.8+jda-5.0.0") {
        exclude(module = "opus-java")
    }
    implementation(libs.javalin.bundle)
    implementation("net.lingala.zip4j", "zip4j", "2.11.5")

    // database
    implementation(libs.bundles.sadu)
    implementation("org.postgresql", "postgresql", "42.7.4")

    // Logging
    implementation(libs.bundles.logging)
    implementation("club.minnced", "discord-webhooks", "0.8.4")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.11.2"))
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
