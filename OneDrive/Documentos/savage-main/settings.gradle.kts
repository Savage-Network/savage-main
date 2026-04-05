plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "minecraft"

include("core")
include("core-bungee")
include("core-bukkit")

// Useful
include("lobby")
include("auth")

// Games
include("duels")
include("pvp")
include("bedwars")
include("aquatic")
include("hungergames")
include("ponte")

// API
include("savage-api")
