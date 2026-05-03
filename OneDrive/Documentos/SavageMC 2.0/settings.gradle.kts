plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Stop"

include("core")
include("core-bukkit")
include("core-bungee")
include("lobby")
include("auth")
include("duels")
include("pvp")
include("bedwars")
include("skywars")
include("eggwars")
include("thebridge")

include("game")
include("game:hungergames")
include("game:terrain-generator")
