rootProject.name = "gamejam"
include("bot")
include("plugin-api")
include("plugin-paper")
include("plugin-velocity")
include("plugin-paper:Readme.md")
findProject(":plugin-paper:Readme.md")?.name = "Readme.md"
