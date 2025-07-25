import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.2.20-Beta1"
    `maven-publish`
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        binaries.library()
        nodejs()
        useEsModules()
        generateTypeScriptDefinitions()
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {}
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
