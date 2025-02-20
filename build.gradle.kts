plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.github.test.summary"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20240205")
}

intellij {
    version.set("2023.3")
    type.set("PY")  // Use PY for PyCharm
    plugins.set(listOf())  // PyCharm already includes Python plugin
    updateSinceUntilBuild.set(false)
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    processResources {
        from("src/main/resources") {
            include("**/*")
        }
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from("resources") {
            into("")
        }
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
            exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/MANIFEST.MF")
        }
    }

    prepareSandbox {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }) {
            into("${intellij.pluginName.get()}/lib")
        }
    }
}
