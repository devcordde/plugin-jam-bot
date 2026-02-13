plugins {
    java
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.shadow)
    `maven-publish`
    `java-library`
    id("xyz.jpenilla.run-paper") version "3.0.0"
}

group = "de.chojo.pluginjam"
version = "1.0.4"

dependencies {
    implementation(project(":plugin-api"))
    compileOnly(libs.paper)
    bukkitLibrary(libs.javalin.core)
    bukkitLibrary(libs.bundles.eldoutil)
    bukkitLibrary("com.fasterxml.jackson.core", "jackson-databind", "2.21.0")
    api(libs.bundles.eldoutil)
    bukkitLibrary(libs.slf4j)
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }

    register<Copy>("copyToServer") {
        val path = project.property("targetDir") ?: "";
        if (path.toString().isEmpty()) {
            println("targetDir is not set in gradle properties")
            return@register
        }
        from(shadowJar)
        destinationDir = File(path.toString())
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.1")
    }
}

publishing {
    repositories {
        maven {
            name = "Eldonexus"
            url = uri("https://eldonexus.de/repository/maven-releases")
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

bukkit {
    name = "PluginJam"
    main = "de.chojo.pluginjam.PluginJam"
    website = "https://github.com/devcordde/plugin-jam-bot"
    apiVersion = "1.21"
    version = rootProject.version.toString()
    authors = listOf("Taucher2003", "RainbowdashLabs")
}
