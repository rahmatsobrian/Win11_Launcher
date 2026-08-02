pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Win11Launcher"

include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":feature:desktop")
include(":feature:taskbar")
include(":feature:startmenu")
include(":feature:appdrawer")
include(":feature:search")
include(":feature:widgets")
include(":feature:settings")
