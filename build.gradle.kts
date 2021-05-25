val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.0"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")

//    Postgres
    implementation ("com.zaxxer:HikariCP:3.4.5") // JDBC Connection Pool
    implementation("org.postgresql:postgresql:42.2.2")

    //DataBase
    implementation("org.jetbrains.exposed", "exposed-core", "0.31.1")
//  implementation(("org.jetbrains.exposed", "exposed-dao", "0.31.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.31.1")
//    implementation("com.h2database:h2:1.4.199")
    implementation("com.apurebase:kgraphql:0.17.8")
}