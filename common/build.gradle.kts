architectury {
    common(project.property("enabled_platforms").toString().split(","))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    implementation(libs.fabric.loader)
}

val mergedJar = tasks.register<Jar>("mergedJar") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("merged")
}

configurations {
    create("mergedElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("transformProductionFabricElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("transformProductionNeoForgeElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("mergedElements", mergedJar)
    add("transformProductionFabricElements", tasks.named("transformProductionFabric"))
    add("transformProductionNeoForgeElements", tasks.named("transformProductionNeoForge"))
}

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            artifactId = project.property("mod_id") as String
            from(components["java"])
        }
    }
}
