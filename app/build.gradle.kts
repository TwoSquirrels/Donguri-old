/*
 * © 2022 TwoSquirrels
 * This file is licensed under the MIT License, see /LICENSE file.
 *
 * Frequently used tasks
 * - debug      : debug run
 * - clean      : clean build
 * - shadowJar  : build
 * - build      : build and test and dist
 * - run        : run jar
 */

// constants

group = "com.github.twosquirrels"
val packageMain = "$group.donguri.DonguriKt"
version = "1.0.0"

plugins {
    // Kotlin/JVM
    kotlin("jvm") version "1.6.10"
    // build
    application
    distribution
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // define main
    mainClass.set(packageMain)
}

// libraries

repositories {
    mavenCentral()
}

dependencies {
    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    // dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    // logback
    implementation("ch.qos.logback:logback-classic:1.2.8")
    // Java Discord API
    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    // test
    testImplementation(kotlin("test"))
}

// others

// build

val fileNameBase = "${rootProject.name}-${project.name}"
val fileNameVersion = "v$version"

val jar by tasks.getting(Jar::class) {
    archiveBaseName.set(fileNameBase)
    archiveVersion.set(fileNameVersion)
    manifest {
       attributes["Main-Class"] = packageMain
    }
}

val shadowJar by tasks.getting(Jar::class) {
    archiveBaseName.set(fileNameBase)
    archiveVersion.set(fileNameVersion)
}

// dist

distributions {
    main {
        distributionBaseName.set(fileNameBase)
        contents {
            // exclude unshadowed jar
            exclude {
                it.file.toRelativeString(projectDir)
                    .startsWith("build/libs/") &&
                    !it.name.endsWith("-all.jar")
            }
            // exclude libraries
            exclude {
                it.file.toRelativeString(rootDir)
                    .contains("/.gradle/caches/")
            }
            // exclude scripts
            exclude {
                it.file.toRelativeString(projectDir)
                    .startsWith("build/scripts/")
            }
            // shadowed jar
            from(tasks.shadowJar)
            // project specific enclosures
            arrayOf(
                "$rootDir/README.md",
                "$rootDir/LICENSE",
                "$rootDir/.env.template"
            ).forEach { from(it) }
        }
    }
}

// run

tasks.getByName("run", JavaExec::class) {
    workingDir = rootDir
    standardInput = System.`in`
}

// project specific tasks

task("debug", JavaExec::class) {
    workingDir = rootDir
    standardInput = System.`in`
    mainClass.set(packageMain)
    classpath = java.sourceSets["main"].runtimeClasspath
}
