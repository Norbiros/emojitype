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
    "modImplementation"(libs.fabric.loader)
    "modApi"(libs.modmenu)

    include(libs.snakeyaml)

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("fabric")
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
            from(components["java"])
        }
    }
}

