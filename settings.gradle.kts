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
            plugin("shadow", "com.gradleup.shadow").version("8.3.0")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
        }
    }
}
