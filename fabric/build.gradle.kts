architectury {
    platformSetupLoomIde()
    fabric()
}

val commonConfig = configurations.getByName("common")
val shadowCommonConfig = configurations.getByName("shadowCommon")
val developmentFabricConfig = configurations.getByName("developmentFabric")

configurations {
    compileClasspath.get().extendsFrom(commonConfig)
    runtimeClasspath.get().extendsFrom(commonConfig)
    developmentFabricConfig.extendsFrom(commonConfig)
}

dependencies {
    implementation(libs.fabric.loader)
    api(libs.modmenu)

    common(project(path = ":common", configuration = "mergedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabricElements")) { isTransitive = false }
    shadowCommon("org.yaml:snakeyaml:${libs.versions.snakeyaml.get()}")
}

tasks.shadowJar {
    archiveClassifier.set("fabric")
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
        create<MavenPublication>("mavenFabric") {
            artifactId = "${project.property("mod_id")}-fabric"
            artifact(tasks.shadowJar)
            artifact(tasks.sourcesJar)
        }
    }
}
