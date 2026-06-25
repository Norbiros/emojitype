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

    common(project(path = ":common", configuration = "mergedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForgeElements")) { isTransitive = false }
    shadowCommon("org.yaml:snakeyaml:${libs.versions.snakeyaml.get()}")
}

tasks.shadowJar {
    archiveClassifier.set("neoforge")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
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
            artifact(tasks.shadowJar)
            artifact(tasks.sourcesJar)
        }
    }
}
