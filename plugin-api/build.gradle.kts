plugins {
    java
    `java-library`
}

group = "de.chojo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core", "jackson-databind", "2.13.4.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
