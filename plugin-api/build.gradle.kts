plugins {
    java
    `java-library`
    `maven-publish`
}

group = "de.chojo"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core", "jackson-databind", "2.18.3")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

publishing {
    repositories {
        maven {
            name = "Eldonexus"
            url = uri("https://eldonexus.de/repository/maven-releases")
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
