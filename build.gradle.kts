plugins {
    java
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom) apply false
}

architectury {
    minecraft = libs.versions.minecraft.get()
}

val rootLibs = libs

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "dev.architectury.loom")

    val common: Configuration by configurations.creating
    val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
    val compileClasspath: Configuration by configurations.getting
    val runtimeClasspath: Configuration by configurations.getting

    compileClasspath.extendsFrom(common)
    runtimeClasspath.extendsFrom(common)

    configurations.configureEach {
        if (name == "developmentFabric") {
            extendsFrom(common)
        }
    }

    tasks.processResources {
        val resourceTargets = listOf(
            "fabric.mod.json",
            "META-INF/neoforge.mods.toml",
        )

        val replaceProperties = mapOf(
            "version" to project.property("mod_version"),
            "fabric_minecraft_version_range" to project.property("fabric_minecraft_version_range"),
            "neo_version_range" to project.property("neo_minecraft_version_range"),
            "fabric_loader_version" to rootLibs.versions.fabric.loader.get(),
            "modmenu_version" to rootLibs.versions.modmenu.get(),
        )

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }

    dependencies {
        "minecraft"(rootLibs.minecraft)
        implementation(rootLibs.snakeyaml)

        val loom = project.extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
        "mappings"(loom.layered {
            mappings(rootLibs.yarn.mappings.get().toString() + ":v2")
            mappings(rootLibs.neoforge.yarn.mappings.patch.get().toString())
        })
    }

    tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        exclude("architectury.common.json")
        configurations = listOf(shadowCommon)
        archiveClassifier.set("dev-shadow")
    }

    tasks.jar {
        archiveClassifier.set("dev")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName.set(project.property("mod_id") as String)
    version = project.property("mod_version") as String
    group = project.property("maven_group") as String

    repositories {
        // Architectury
        maven("https://maven.shedaniel.me/")
        // ModMenu
        maven("https://maven.terraformersmc.com/")
        // NeoForge
        maven("https://maven.neoforged.net/releases/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}
