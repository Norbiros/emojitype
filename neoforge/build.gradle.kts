architectury {
    platformSetupLoomIde()
    neoForge()
}

val commonConfig = configurations.getByName("common")
val shadowCommonConfig = configurations.getByName("shadowCommon")
val developmentNeoForgeConfig = configurations.maybeCreate("developmentNeoForge")

configurations {
    compileClasspath.get().extendsFrom(commonConfig)
    runtimeClasspath.get().extendsFrom(commonConfig)
    developmentNeoForgeConfig.extendsFrom(commonConfig)
}

dependencies {
    "neoForge"(libs.neoforge)

    include(libs.snakeyaml)

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("neoforge")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenNeoForge") {
            artifactId = "${project.property("mod_id")}-neoforge"
            from(components["java"])
        }
    }
}

