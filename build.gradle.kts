group = "org.bmp.pps.scala-mqtt"
version = "0.0.0"

plugins {
    scala
    application
    id("org.scoverage") version "3.0.0"
    id("com.github.maiflai.scalatest") version "0.21"
}

application {
    mainClassName = "HelloWorld"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.12.2")
    testImplementation("org.scalatest:scalatest_2.12:3.0.1")
    testRuntimeOnly("org.pegdown:pegdown:1.4.2")
    scoverage("org.scoverage:scalac-scoverage-plugin_2.12:1.3.1")
    scoverage("org.scoverage:scalac-scoverage-runtime_2.12:1.3.1")
}