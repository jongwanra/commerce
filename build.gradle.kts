plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // For optimistic lock
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")

    // Redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.35.0")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")


    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")


    // DB
    runtimeOnly("com.mysql:mysql-connector-j")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility:4.2.0") // 비동기 테스트 목적


    // Open API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    /**
     * querydsl
     */

    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
}

/**
 * querydsl build options
 */
val querydslSrcDir = "src/main/generated"
sourceSets["main"].java.srcDir(querydslSrcDir)

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("user.timezone", "UTC")
    systemProperty("file.encoding", "UTF-8") // Warning:(55, 39) Non-ASCII characters
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory = file(querydslSrcDir)
    options.encoding = "UTF-8" // Warning:(55, 39) Non-ASCII characters
}

tasks.named("clean") {
    doLast {
        file(querydslSrcDir).deleteRecursively()
    }
}

tasks.bootJar {
    archiveFileName = "app.jar"
    destinationDirectory = file("./docker/app")
}
