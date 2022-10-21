plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories{
    maven("https://repo.velocitypowered.com/snapshots/")
}

dependencies{
    implementation("io.javalin", "javalin", "4.6.5")
    compileOnly("com.velocitypowered", "velocity-api", "1.0.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "1.0.0-SNAPSHOT")
}

tasks{
    shadowJar {
        val shadebase = "de.chojo.pluginjam.libs"
        //relocate("io.javalin", "$shadebase.javalin")
        //relocate("org.eclipse", shadebase)
        mergeServiceFiles()
        archiveBaseName.set("PluginJam")
        archiveClassifier.set("")
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
