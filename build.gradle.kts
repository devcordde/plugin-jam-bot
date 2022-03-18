plugins {
    java
    id("org.cadixdev.licenser") version "0.6.1"
}

group = "de.chojo"
version = "1.0"

subprojects {
    apply {
        plugin<org.cadixdev.gradle.licenser.Licenser>()
    }
}

allprojects {
    license {
        header(rootProject.file("HEADER.txt"))
        include("**/*.java")
    }
}
