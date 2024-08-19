import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
    java
    alias(libs.plugins.spotless)
}

group = "de.chojo"
version = "1.0.0"

subprojects {
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
    }

    apply {
        plugin<JavaPlugin>()
        plugin<SpotlessPlugin>()
    }


    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER.txt"))
            target("**/*.java")
        }
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
            dependsOn(spotlessCheck)
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
