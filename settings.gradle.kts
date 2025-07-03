pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    versionCatalogs {
        /*libs {
            from(files("gradle/libs.versions.toml"))
        }*/
        create("testLibs") {
            from(files("gradle/libs-test.versions.toml"))
        }
    }

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Banter Box"
include(":app")
include(":core:analytics")
include(":core:common")
include(":core:datastore")

