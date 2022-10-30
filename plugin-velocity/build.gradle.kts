plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories{
    maven("https://repo.velocitypowered.com/snapshots/")
}

dependencies{
    implementation("io.javalin", "javalin", "4.6.5")
    implementation(project(":plugin-api"))
    compileOnly("com.velocitypowered", "velocity-api", "1.0.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "1.0.0-SNAPSHOT")
}

tasks{
    shadowJar {
        val shadebase = "de.chojo.pluginjam.libs"
        //relocate("io.javalin", "$shadebase.javalin")
        //relocate("org.eclipse", shadebase)
        mergeServiceFiles()
        archiveFileName.set("PluginJam.jar")
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
}
