plugins {
    java
    alias(libs.plugins.shadow)
}

version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies{
    implementation(project(":plugin-api"))
    implementation(libs.javalin.core)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}

tasks {
    shadowJar {
        val shadebase = "de.chojo.pluginjam.libs"
        //relocate("io.javalin", "$shadebase.javalin")
        //relocate("org.eclipse", shadebase)
        mergeServiceFiles()
    }

    build{
        dependsOn(shadowJar)
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
