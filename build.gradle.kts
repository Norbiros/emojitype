plugins {
    java
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.mod.publish.plugin)
}

architectury {
    minecraft = libs.versions.minecraft.get()
}

val rootLibs = libs

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "dev.architectury.loom-no-remap")

    val common = configurations.create("common")
    val shadowCommon = configurations.create("shadowCommon") // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
    val compileClasspath = configurations.named("compileClasspath")
    val runtimeClasspath = configurations.named("runtimeClasspath")

    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }

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
        noIntermediateMappings()
        silentMojangMappingsLicense()
    }

    dependencies {
        "minecraft"(rootLibs.minecraft)
        implementation(rootLibs.snakeyaml)

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
        options.release.set(project.property("java_version").toString().toInt())
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}

publishMods {
    changelog = providers.environmentVariable("CHANGELOG").orElse("")
    type = STABLE

    val fabricJar = project(":fabric").tasks.named<Jar>("shadowJar")
    val neoforgeJar = project(":neoforge").tasks.named<Jar>("shadowJar")
    val mcVersion = libs.versions.minecraft.get()

    val modrinthOptions = modrinthOptions {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "q7vRRpxU"
        minecraftVersions.add(mcVersion)
    }

    modrinth("modrinthFabric") {
        from(modrinthOptions)
        file = fabricJar.flatMap { it.archiveFile }
        modLoaders.add("fabric")
    }

    modrinth("modrinthNeoForge") {
        from(modrinthOptions)
        file = neoforgeJar.flatMap { it.archiveFile }
        modLoaders.add("neoforge")
    }

    val curseforgeOptions = curseforgeOptions {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "574752"
        minecraftVersions.add(mcVersion)
        client = true
        server = true
    }

    curseforge("curseforgeFabric") {
        from(curseforgeOptions)
        file = fabricJar.flatMap { it.archiveFile }
        modLoaders.add("fabric")
    }

    curseforge("curseforgeNeoForge") {
        from(curseforgeOptions)
        file = neoforgeJar.flatMap { it.archiveFile }
        modLoaders.add("neoforge")
    }
}
