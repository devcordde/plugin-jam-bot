plugins {
    java
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies{
    implementation("io.javalin", "javalin", "4.6.8")
    implementation(project(":plugin-api"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

tasks {
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
