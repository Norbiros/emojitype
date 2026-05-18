plugins {
    java
    alias(libs.plugins.fabric.loom) apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    base.archivesName.set(project.property("mod_id") as String)
    version = project.property("mod_version") as String
    group = project.property("maven_group") as String

    repositories {
        // ModMenu
        maven("https://maven.terraformersmc.com/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}
