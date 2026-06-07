plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.testng:testng:7.9.0")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.13.0")
    testImplementation("io.appium:java-client:9.2.0")
}

tasks.test {
    useTestNG()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
