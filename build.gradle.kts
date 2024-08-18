plugins {
    java
    id("org.cadixdev.licenser") version "0.6.1"
}

group = "de.chojo"
version = "1.0.0"

subprojects {
    apply {
        plugin<org.cadixdev.gradle.licenser.Licenser>()
        plugin<JavaPlugin>()
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
    }

    license {
        header(rootProject.file("HEADER.txt"))
        include("**/*.java")
    }

    java {
        withSourcesJar()
        withJavadocJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks {
        test {
            dependsOn(licenseCheck)
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }

        compileJava {
            options.encoding = "UTF-8"
        }

        javadoc {
            options.encoding = "UTF-8"
        }
    }
}
