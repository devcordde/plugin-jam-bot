plugins {
    `java-library`
}

group = "de.chojo"
version = "2.1.2"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "20.1.0")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_15
        withSourcesJar()
        withJavadocJar()
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }
}
