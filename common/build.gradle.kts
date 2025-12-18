architectury {
    common(project.property("enabled_platforms").toString().split(","))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    "modImplementation"(libs.fabric.loader)
}

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            artifactId = project.property("mod_id") as String
            from(components["java"])
        }
    }
}

