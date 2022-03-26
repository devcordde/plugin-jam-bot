plugins {
    java
}

group = "de.chojo"
version = "1.0"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // discord
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.9") {
        exclude(module = "opus-java")
    }

    implementation("de.chojo", "cjda-util", "2.2.0q+alpha.9-SNAPSHOT")


    // database
    implementation("de.chojo", "sql-util", "1.2.1")
    implementation("org.postgresql", "postgresql", "42.3.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.36")
    implementation("org.apache.logging.log4j", "log4j-core", "2.17.2")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.17.2")
    implementation("club.minnced", "discord-webhooks", "0.7.5")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
