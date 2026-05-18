plugins {
    alias(libs.plugins.fabric.loom)
}

sourceSets {
    main {
        java.srcDir(rootProject.file("common/src/main/java"))
        java.exclude(
            "dev/norbiros/emojitype/config/ui/ConfirmationDialog.java",
            "dev/norbiros/emojitype/config/ui/EmojiListWidget.java",
            "dev/norbiros/emojitype/config/ui/PackEditorScreen.java",
            "dev/norbiros/emojitype/config/ui/PackListWidget.java",
            "dev/norbiros/emojitype/config/ui/PackPropertiesDialog.java",
            "dev/norbiros/emojitype/config/ui/UIColors.java",
        )
        resources.srcDir(rootProject.file("common/src/main/resources"))
    }
}

dependencies {
    add("minecraft", libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.modmenu)
    implementation(libs.snakeyaml)
    include(libs.snakeyaml)
}

tasks.processResources {
    val replaceProperties = mapOf(
        "version" to project.property("mod_version"),
        "fabric_minecraft_version_range" to project.property("fabric_minecraft_version_range"),
        "fabric_loader_version" to libs.versions.fabric.loader.get(),
        "modmenu_version" to libs.versions.modmenu.get(),
    )

    inputs.properties(replaceProperties)
    filesMatching("fabric.mod.json") {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenFabric") {
            artifactId = "${project.property("mod_id")}-fabric"
            from(components["java"])
        }
    }
}

