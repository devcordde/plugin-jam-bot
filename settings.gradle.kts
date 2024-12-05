rootProject.name = "gamejam"
include("bot")
include("plugin-api")
include("plugin-paper")
include("plugin-velocity")
include("plugin-paper:Readme.md")
findProject(":plugin-paper:Readme.md")?.name = "Readme.md"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("sadu", "2.3.1")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            bundle("sadu", listOf("sadu-queries", "sadu-updater", "sadu-datasource", "sadu-postgresql"))

            version("log4j", "2.24.2")
            library("slf4j", "org.slf4j:slf4j-api:2.0.16")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            bundle("logging", listOf("slf4j", "log4j-core", "log4j-slf4j2"))


            version("javalin", "4.6.8")
            library("javalin-core", "io.javalin", "javalin").versionRef("javalin")
            library("javalin-bundle", "io.javalin", "javalin-bundle").versionRef("javalin")

            version("eldoutil", "2.1.7")
            library("eldoutil-plugin", "de.eldoria.util", "plugin").versionRef("eldoutil")
            bundle("eldoutil", listOf("eldoutil-plugin"))

            library("velocity", "com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
            library("paper", "io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
            plugin("shadow", "com.gradleup.shadow").version("8.3.5")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
            plugin("spotless", "com.diffplug.spotless").version("6.25.0")
        }
    }
}
