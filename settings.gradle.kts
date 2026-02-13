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
            version("sadu", "2.3.5")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            bundle("sadu", listOf("sadu-queries", "sadu-updater", "sadu-datasource", "sadu-postgresql"))

            version("log4j", "2.25.3")
            library("slf4j", "org.slf4j:slf4j-api:2.0.17")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            bundle("logging", listOf("slf4j", "log4j-core", "log4j-slf4j2"))


            version("javalin", "6.7.0-5")
            library("javalin-core", "io.javalin", "javalin").versionRef("javalin")
            library("javalin-bundle", "io.javalin", "javalin-bundle").versionRef("javalin")
            library("javalin-annotation", "io.javalin.community.openapi", "openapi-annotation-processor").versionRef("javalin")
            library("javalin-openapi", "io.javalin.community.openapi", "javalin-openapi-plugin").versionRef("javalin")
            library("javalin-swagger", "io.javalin.community.openapi", "javalin-swagger-plugin").versionRef("javalin")

            version("eldoutil", "2.1.11")
            library("eldoutil-plugin", "de.eldoria.util", "plugin").versionRef("eldoutil")
            bundle("eldoutil", listOf("eldoutil-plugin"))

            library("velocity", "com.velocitypowered:velocity-api:3.4.0")
            library("paper", "io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
            plugin("shadow", "com.gradleup.shadow").version("9.3.1")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
            plugin("spotless", "com.diffplug.spotless").version("8.2.1")
        }
    }
}
