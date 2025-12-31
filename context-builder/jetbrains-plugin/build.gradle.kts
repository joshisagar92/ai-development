// =============================================================
// PLUGINS
// =============================================================
// The intellij plugin handles:
// - Downloading IntelliJ Platform SDK
// - Running a sandboxed IDE for testing
// - Building the plugin .zip for distribution
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

// =============================================================
// PROJECT CONFIGURATION
// =============================================================
group = "com.contextbuilder"
version = "1.0.0"

// =============================================================
// REPOSITORIES
// =============================================================
repositories {
    mavenCentral()
}

// =============================================================
// JAVA CONFIGURATION
// =============================================================
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// =============================================================
// INTELLIJ PLUGIN CONFIGURATION
// =============================================================
intellij {
    // Version of IntelliJ IDEA to build against
    // Use "LATEST-EAP-SNAPSHOT" for latest, or specific like "2024.1"
    version.set("2024.1")

    // Type: IC = Community, IU = Ultimate
    type.set("IC")

    // Plugins this plugin depends on (none for now)
    plugins.set(listOf())
}

// =============================================================
// TASKS
// =============================================================
tasks {
    // Compiler options
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    // Plugin metadata patching
    patchPluginXml {
        // Minimum IDE version (e.g., 231 = 2023.1)
        sinceBuild.set("231")
        // Maximum IDE version (empty = no limit)
        untilBuild.set("243.*")
    }

    // Sign plugin (optional, for marketplace)
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    // Publish plugin (optional, for marketplace)
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
