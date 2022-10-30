plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
}

group = "de.chojo"
version = "1.0"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
    mavenCentral()
}

dependencies {
    implementation(project(":plugin-api"))

    // discord
    implementation("de.chojo", "cjda-util", "2.7.8+alpha.22-DEV") {
        exclude(module = "opus-java")
    }
    implementation("io.javalin", "javalin-bundle", "4.4.0")
    implementation("net.lingala.zip4j", "zip4j", "2.11.2")

    // database
    implementation("de.chojo.sadu", "sadu-queries", "1.2.0")
    implementation("de.chojo.sadu", "sadu-updater", "1.2.0")
    implementation("de.chojo.sadu", "sadu-datasource", "1.2.0")
    implementation("de.chojo.sadu", "sadu-postgresql", "1.2.0")
    implementation("org.postgresql", "postgresql", "42.3.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "2.0.3")
    implementation("org.apache.logging.log4j", "log4j-core", "2.19.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.19.0")
    implementation("club.minnced", "discord-webhooks", "0.7.5")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
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
