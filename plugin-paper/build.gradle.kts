plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.chojo"

dependencies {
    implementation(project(":plugin-api"))
    compileOnly("io.papermc.paper", "paper-api", "1.19.2-R0.1-SNAPSHOT")
    implementation("io.javalin", "javalin", "4.6.8")
    implementation("de.eldoria", "eldo-util", "1.14.5")
    implementation("org.slf4j", "slf4j-api", "1.7.36")
}

tasks {
    shadowJar {
        val shadebase = "de.chojo.pluginjam."
        relocate("de.eldoria.eldoutilities", shadebase + "eldoutilities")
        //relocate("io.javalin", shadebase + "javalin")
        mergeServiceFiles()
        archiveFileName.set("pluginjam.jar")
        //minimize()
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

    build{
        dependsOn(shadowJar)
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
