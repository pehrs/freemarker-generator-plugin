/*
 * Copyright (c) 2024. Matti Pehrs (matti@pehrs.com)
 */


plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
    // id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.pehrs"
version = "1.0.0"


repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

dependencies {


    implementation("org.freemarker:freemarker:2.3.33")

    // FIXME: Cannot use velocity as it will clash with the embedded version
    // implementation("org.apache.velocity:velocity:1.7")

    // StringTemplate does not support calling methods on variables :-(
    // implementation("org.antlr:ST4:4.3.4")

    // Cannot use later versions of Velocity :-(
    //    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")  {
    //        exclude(group = "org.slf4j")
    //    }
    //    implementation("org.apache.velocity.tools:velocity-tools-view:3.1")  {
    //        exclude(group = "org.slf4j")
    //    }

    // Junit 5 is NOT supported by intellij SDK :-(
    //    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    //    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    
    // testImplementation("junit:junit:4.13")
    //    testImplementation("org.hamcrest:hamcrest-core:1.3")
    //    testImplementation("org.hamcrest:hamcrest-library:1.3")

    // We CAN use mockito for junit4 unit tests :-)
    testImplementation("org.mockito:mockito-core:5.12.0")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.1.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))

}


tasks {

    // apply(plugin = "com.github.johnrengelman.shadow")

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

//    shadowJar {
//        archiveClassifier.set("") // No classifier
//        relocate("org.apache.velocity", "com.pehrs.org.apache.velocity") // Adjust the package relocation as needed
//
//        dependencies {
//            include(dependency("org.apache.velocity:velocity"))
//            // Add other dependencies to be shaded here
//        }
//    }

//    buildSearchableOptions {
//        dependsOn(shadowJar)
//    }
//
//    runIde {
//        dependsOn(shadowJar)
//    }
//
//    verifyPlugin {
//        dependsOn(shadowJar)
//    }
//
//    buildPlugin {
//        dependsOn(shadowJar)
//    }
//
//    // Make the build task depend on the shadowJar task
//    build {
//        dependsOn(shadowJar)
//    }
//
//    named<org.jetbrains.intellij.tasks.PrepareSandboxTask>("prepareSandbox") {
//        dependsOn(shadowJar)
//        from(shadowJar.get().archiveFile.get().asFile) {
//            into("${project.name}/lib")
//        }
//    }
}

//
//tasks.named<Jar>("jar") {
//    // Disable the default JAR task to prevent creating an unshadowed JAR
//    enabled = false
//}
