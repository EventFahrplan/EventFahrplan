pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "de.mobilej.unmock") {
                def version = requested.version
                if (version == null) {
                    throw new GradleException("Plugin de.mobilej.unmock must declare a version so it can be resolved as com.github.bjoernq:unmockplugin.")
                }
                useModule("com.github.bjoernq:unmockplugin:$version")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("config") {
            from(files("gradle/config.versions.toml"))
        }
    }
}

include(":app", ":commons", ":commons-testing", ":database", ":network")
