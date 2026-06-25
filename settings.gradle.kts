pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("neoforge")

rootProject.name = "emojitype"

