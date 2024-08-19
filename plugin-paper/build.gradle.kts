plugins {
    java
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.shadow)
    `maven-publish`
}

group = "de.chojo"
version = "1.0.1"

dependencies {
    implementation(project(":plugin-api"))
    compileOnly(libs.paper)
    implementation(libs.javalin.core)
    implementation(libs.eldoutil)
    implementation(libs.slf4j)
}

tasks {
    shadowJar {
        val shadebase = "de.chojo.pluginjam."
        relocate("de.eldoria.eldoutilities", shadebase + "eldoutilities")
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
    apiVersion = "1.19"
    version = rootProject.version.toString()
    authors = listOf("Taucher2003", "RainbowdashLabs")
}
